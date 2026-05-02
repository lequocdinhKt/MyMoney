package com.example.mymoney.data.repository

import android.util.Log
import com.example.mymoney.data.local.dao.CategoryDao
import com.example.mymoney.data.local.dao.WalletDao
import com.example.mymoney.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repository giao tiếp với Supabase cho bảng "transactions".
 * Dùng trong AddTransactionViewModel (insert đơn) và SettingViewModel (backup batch).
 */
class SupabaseTransactionRepository(
    private val categoryDao: CategoryDao? = null,
    private val walletDao: WalletDao? = null
) {

    private val TAG = "SupabaseTxRepo"

    // ── DTOs ──────────────────────────────────────────────────────────────────

    @Serializable
    private data class TransactionUpsertDto(
        @SerialName("user_id")          val userId: String,
        @SerialName("wallet_id")        val walletId: String,
        @SerialName("category_id")      val categoryId: String,
        val note: String,
        val amount: Double,
        val type: String,
        @SerialName("transaction_date") val transactionDate: String,
        @SerialName("ai_generated")     val aiGenerated: Boolean = true,
        @SerialName("created_at")       val createdAt: String
    )

    @Serializable
    private data class RemoteCategoryDto(
        val id: String,
        val name: String
    )

    @Serializable
    private data class CategoryUpsertDto(
        @SerialName("user_id")   val userId: String,
        val name: String,
        val type: String,
        val icon: String,
        val color: String,
        @SerialName("is_system") val isSystem: Boolean = false
    )

    @Serializable
    private data class RemoteWalletDto(
        val id: String,
        val name: String
    )

    @Serializable
    private data class WalletUpsertDto(
        @SerialName("user_id")    val userId: String,
        val name: String,
        val balance: Double,
        val icon: String,
        val color: String,
        @SerialName("is_default") val isDefault: Boolean = false
    )

    // ── Single-insert (AI chat) ───────────────────────────────────────────────

    /**
     * Insert 1 giao dịch mới từ AI chat lên Supabase.
     * @param walletSupabaseId UUID ví trong Supabase — null nếu ví chưa sync.
     * @return true nếu thành công
     */
    suspend fun insertTransaction(
        userId: String,
        note: String,
        amount: Double,
        type: String,
        category: String,
        timestampMillis: Long,
        walletSupabaseId: String?
    ): Boolean {
        if (walletSupabaseId == null) {
            Log.d(TAG, "Skip Supabase insert: wallet not synced yet (supabase_id = null)")
            return false
        }
        return try {
            val categoryMap = fetchCategoryMap()
            val catId = categoryMap[category] ?: categoryMap["Khác"]
            if (catId == null) {
                Log.w(TAG, "Skip Supabase insert: category '$category' not found in Supabase")
                return false
            }
            val iso = millisToIso(timestampMillis)
            val dto = TransactionUpsertDto(
                userId          = userId,
                walletId        = walletSupabaseId,
                categoryId      = catId,
                note            = note,
                amount          = amount,
                type            = type,
                transactionDate = iso,
                aiGenerated     = true,
                createdAt       = iso
            )
            SupabaseClient.client.postgrest["transactions"].insert(dto)
            Log.d(TAG, "Inserted 1 transaction to Supabase: $note $amount")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Insert failed: ${e.message}", e)
            false
        }
    }

    // ── Batch backup (SettingViewModel) ───────────────────────────────────────

    /**
     * Backup toàn bộ giao dịch Room lên Supabase.
     * Auto-sync categories & wallets trước nếu chưa có trên Supabase.
     * @return số giao dịch upload thành công
     */
    suspend fun upsertAll(userId: String, transactions: List<TransactionItem>): Int {
        if (transactions.isEmpty()) return 0

        // 1. Restore JWT session
        try {
            SupabaseClient.client.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d(TAG, "Session restored for backup")
        } catch (e: Exception) {
            Log.w(TAG, "Could not restore session: ${e.message}")
        }

        // 2. Sync categories → gets name→UUID map
        val categoryMap: Map<String, String> = try {
            syncCategoriesToSupabase(userId)
            fetchCategoryMap()
        } catch (e: Exception) {
            Log.e(TAG, "Category sync/fetch FAILED: ${e.message}", e)
            throw Exception("Không thể đồng bộ danh mục lên Supabase: ${e.message}")
        }

        val fallbackId: String? = categoryMap["Khác"]
        Log.d(TAG, "Category map size: ${categoryMap.size}, fallback 'Khác' = $fallbackId")

        // 3. Sync wallets → gets localId→supabaseId map
        val walletMap: Map<Long, String> = try {
            syncWalletsToSupabase(userId)
        } catch (e: Exception) {
            Log.w(TAG, "Wallet sync failed (non-fatal): ${e.message}")
            emptyMap()
        }
        Log.d(TAG, "Wallet map: $walletMap")

        // 4. Build DTOs
        val dtos = transactions.mapNotNull { tx ->
            val catId = categoryMap[tx.category] ?: fallbackId
            if (catId == null) {
                Log.w(TAG, "Skipping '${tx.note}': category '${tx.category}' not in Supabase")
                return@mapNotNull null
            }
            // resolve wallet: prefer already-known supabaseId, else look up from map
            val walletSupabaseId = tx.walletSupabaseId ?: walletMap[tx.walletId]
            if (walletSupabaseId == null) {
                Log.w(TAG, "Skipping '${tx.note}': wallet ${tx.walletId} not synced to Supabase")
                return@mapNotNull null
            }
            TransactionUpsertDto(
                userId          = userId,
                walletId        = walletSupabaseId,
                categoryId      = catId,
                note            = tx.note,
                amount          = tx.amount,
                type            = tx.type,
                transactionDate = millisToIso(tx.timestampMillis),
                aiGenerated     = tx.aiGenerated,
                createdAt       = millisToIso(tx.timestampMillis)
            )
        }

        if (dtos.isEmpty()) {
            Log.w(TAG, "No valid DTOs after sync. Available categories: ${categoryMap.keys}")
            return 0
        }

        // 5. Batch insert; fallback to one-by-one on error
        return try {
            SupabaseClient.client.postgrest["transactions"].insert(dtos)
            Log.d(TAG, "Batch inserted ${dtos.size} transactions")
            dtos.size
        } catch (e: Exception) {
            Log.e(TAG, "Batch insert failed, trying one-by-one: ${e.message}")
            var count = 0
            dtos.forEach { dto ->
                try {
                    SupabaseClient.client.postgrest["transactions"].insert(dto)
                    count++
                } catch (ex: Exception) {
                    Log.w(TAG, "Skip 1 transaction: ${ex.message}")
                }
            }
            Log.d(TAG, "Fallback result: $count/${dtos.size} inserted")
            count
        }
    }

    // ── Sync helpers ──────────────────────────────────────────────────────────

    /**
     * Upload local categories (chưa có supabase_id) lên Supabase.
     * Non-fatal: ghi log nhưng không throw.
     */
    private suspend fun syncCategoriesToSupabase(userId: String) {
        val dao = categoryDao ?: return
        val pending = dao.getPendingSync(userId)
        if (pending.isEmpty()) {
            Log.d(TAG, "No pending categories to sync")
            return
        }
        val dtos = pending.map { cat ->
            CategoryUpsertDto(
                userId   = userId,
                name     = cat.name,
                type     = cat.type,
                icon     = cat.icon,
                color    = cat.color,
                isSystem = cat.isSystem
            )
        }
        try {
            SupabaseClient.client.postgrest["categories"].insert(dtos)
            Log.d(TAG, "Pushed ${dtos.size} categories to Supabase")
        } catch (e: Exception) {
            Log.w(TAG, "Category push failed (will retry on next backup): ${e.message}")
        }
    }

    /**
     * Fetch tất cả categories từ Supabase → map name→UUID.
     * Cũng cập nhật Room cache (supabase_id) nếu có categoryDao.
     */
    private suspend fun fetchCategoryMap(): Map<String, String> {
        val remoteCats = SupabaseClient.client.postgrest["categories"]
            .select()
            .decodeList<RemoteCategoryDto>()

        Log.d(TAG, "Fetched ${remoteCats.size} categories from Supabase: ${remoteCats.map { "'${it.name}'" }}")

        if (categoryDao != null) {
            remoteCats.forEach { remote ->
                categoryDao.updateSupabaseIdByName(remote.name, remote.id)
            }
        }

        return remoteCats.associate { it.name to it.id }
    }

    /**
     * Upload local wallets (chưa có supabase_id) lên Supabase.
     * Sau đó query lại để lấy UUID, cập nhật Room.
     * @return Map<localWalletId, supabaseId>
     */
    private suspend fun syncWalletsToSupabase(userId: String): Map<Long, String> {
        val dao = walletDao ?: return emptyMap()

        // Upload wallets chưa sync
        val pending = dao.getPendingSync(userId)
        if (pending.isNotEmpty()) {
            val dtos = pending.map { w ->
                WalletUpsertDto(
                    userId    = userId,
                    name      = w.name,
                    balance   = w.balance,
                    icon      = w.icon,
                    color     = w.color,
                    isDefault = w.isDefault
                )
            }
            try {
                SupabaseClient.client.postgrest["wallets"].insert(dtos)
                Log.d(TAG, "Pushed ${dtos.size} wallets to Supabase")
            } catch (e: Exception) {
                Log.w(TAG, "Wallet push failed: ${e.message}")
            }
        }

        // Query lại Supabase để lấy UUID (RLS tự filter theo user)
        val remoteWallets = try {
            SupabaseClient.client.postgrest["wallets"]
                .select()
                .decodeList<RemoteWalletDto>()
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch wallets from Supabase: ${e.message}")
            return emptyMap()
        }

        Log.d(TAG, "Fetched ${remoteWallets.size} wallets from Supabase")

        // Match by name, update Room, build result map
        val nameToSupabaseId = remoteWallets.associate { it.name to it.id }
        val allLocalWallets = dao.getWalletsByUser(userId)
        val resultMap = mutableMapOf<Long, String>()

        allLocalWallets.forEach { local ->
            val supabaseId = local.supabaseId ?: nameToSupabaseId[local.name]
            if (supabaseId != null) {
                resultMap[local.id] = supabaseId
                if (local.supabaseId == null) {
                    // Update Room với supabase_id mới lấy được
                    dao.markSynced(local.id, supabaseId)
                }
            }
        }

        return resultMap
    }

    /** epoch millis → ISO 8601 string */
    private fun millisToIso(millis: Long): String =
        java.time.Instant.ofEpochMilli(millis).toString()

    /** Lightweight model truyền dữ liệu từ Room vào repository khi backup */
    data class TransactionItem(
        val note: String,
        val amount: Double,
        val type: String,
        val category: String,
        val timestampMillis: Long,
        val walletId: Long = 0L,            // local Room ID — dùng để lookup supabaseId
        val walletSupabaseId: String? = null, // nếu đã biết trước (AI insert)
        val aiGenerated: Boolean = false
    )
}
