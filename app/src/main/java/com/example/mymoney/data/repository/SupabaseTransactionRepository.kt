package com.example.mymoney.data.repository

import android.util.Log
import com.example.mymoney.data.local.dao.CategoryDao
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
    private val categoryDao: CategoryDao? = null
) {

    private val TAG = "SupabaseTxRepo"

    @Serializable
    private data class TransactionUpsertDto(
        @SerialName("user_id")     val userId: String,
        val note: String,
        val amount: Double,
        val type: String,
        @SerialName("category_id") val categoryId: String,
        @SerialName("created_at")  val createdAt: String   // ISO 8601
    )

    @Serializable
    private data class RemoteCategoryDto(
        val id: String,
        val name: String
    )

    /**
     * Insert 1 giao dịch mới từ AI chat lên Supabase.
     * @return true nếu thành công, false nếu lỗi (non-fatal — không crash app)
     */
    suspend fun insertTransaction(
        userId: String,
        note: String,
        amount: Double,
        type: String,
        category: String,
        timestampMillis: Long
    ): Boolean = try {
        val dto = TransactionUpsertDto(
            userId     = userId,
            note       = note,
            amount     = amount,
            type       = type,
            categoryId = category,
            createdAt  = millisToIso(timestampMillis)
        )
        SupabaseClient.client.postgrest["transactions"].insert(dto)
        Log.d(TAG, "Inserted 1 transaction to Supabase: $note $amount")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Insert failed: ${e.message}", e)
        false
    }

    /**
     * Fetch tất cả categories từ Supabase, trả về map name → UUID.
     * Luôn fetch trực tiếp từ Supabase, không phụ thuộc Room seed data.
     */
    private suspend fun fetchCategoryMap(): Map<String, String> {
        val remoteCats = SupabaseClient.client.postgrest["category"]
            .select()
            .decodeList<RemoteCategoryDto>()

        Log.d(TAG, "Fetched ${remoteCats.size} categories from Supabase: ${remoteCats.map { "'${it.name}'" }}")

        // Cũng update Room cache nếu có categoryDao
        if (categoryDao != null) {
            remoteCats.forEach { remote ->
                categoryDao.updateSupabaseIdByName(remote.name, remote.id)
            }
        }

        return remoteCats.associate { it.name to it.id }
    }

    /**
     * Batch upsert toàn bộ danh sách giao dịch từ Room lên Supabase (dùng cho backup).
     * Tự động resolve category name → supabaseId (UUID) trước khi insert.
     * @return Số lượng giao dịch đã upload thành công
     */
    suspend fun upsertAll(
        userId: String,
        transactions: List<TransactionItem>
    ): Int {
        if (transactions.isEmpty()) return 0

        // ── Restore session để JWT hợp lệ ──
        try {
            SupabaseClient.client.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d(TAG, "Session restored for backup")
        } catch (e: Exception) {
            Log.w(TAG, "Could not restore session: ${e.message}")
        }

        // ── Fetch category name→UUID map trực tiếp từ Supabase ──
        val categoryMap: Map<String, String> = try {
            fetchCategoryMap()
        } catch (e: Exception) {
            Log.e(TAG, "fetchCategoryMap FAILED: ${e.message}", e)
            throw Exception("Không thể tải danh mục từ Supabase: ${e.message}")
        }

        val fallbackId: String? = categoryMap["Khác"]
        Log.d(TAG, "Fallback 'Khác' = $fallbackId")

        val dtos = transactions.mapNotNull { tx ->
            val catId = categoryMap[tx.category] ?: fallbackId
            if (catId == null) {
                Log.w(TAG, "Skipping '${tx.note}': category '${tx.category}' not in Supabase and no fallback")
                return@mapNotNull null
            }
            if (categoryMap[tx.category] == null) {
                Log.w(TAG, "Tx '${tx.note}': '${tx.category}' not found, using 'Khác' fallback")
            }
            TransactionUpsertDto(
                userId     = userId,
                note       = tx.note,
                amount     = tx.amount,
                type       = tx.type,
                categoryId = catId,
                createdAt  = millisToIso(tx.timestampMillis)
            )
        }

        if (dtos.isEmpty()) {
            Log.w(TAG, "No valid DTOs — available Supabase categories: ${categoryMap.keys}")
            return 0
        }

        return try {
            SupabaseClient.client.postgrest["transactions"].insert(dtos)
            Log.d(TAG, "Batch inserted ${dtos.size} transactions to Supabase")
            dtos.size
        } catch (e: Exception) {
            Log.e(TAG, "Batch insert failed, trying one-by-one: ${e.message}")
            var successCount = 0
            dtos.forEach { dto ->
                try {
                    SupabaseClient.client.postgrest["transactions"].insert(dto)
                    successCount++
                } catch (ex: Exception) {
                    Log.w(TAG, "Skip 1 transaction: ${ex.message}")
                }
            }
            Log.d(TAG, "Fallback result: $successCount/${dtos.size} inserted")
            successCount
        }
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
        val timestampMillis: Long
    )
}
