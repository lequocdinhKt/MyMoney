package com.example.mymoney.data.repository

import android.util.Log
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

/**
 * Repository tập trung xử lý đồng bộ hóa toàn bộ dữ liệu từ Room lên Supabase.
 */
class SupabaseSyncRepository(private val db: AppDatabase) {

    private val TAG = "SupabaseSyncRepo"

    // ── DTOs ──────────────────────────────────────────────────────────────────

    @Serializable
    private data class CategoryUpsertDto(
        val id: String? = null,
        @SerialName("user_id")   val userId: String,
        val name: String,
        val type: String,
        val icon: String,
        val color: String,
        @SerialName("is_system") val isSystem: Boolean = false,
        @SerialName("is_deleted") val isDeleted: Boolean = false
    )

    @Serializable
    private data class WalletUpsertDto(
        val id: String? = null,
        @SerialName("user_id")    val userId: String,
        val name: String,
        val balance: Double,
        val icon: String,
        val color: String,
        @SerialName("is_default") val isDefault: Boolean = false,
        @SerialName("is_deleted") val isDeleted: Boolean = false
    )

    @Serializable
    private data class SavingUpsertDto(
        val id: String? = null,
        @SerialName("user_id")       val userId: String,
        val name: String,
        @SerialName("target_amount")  val targetAmount: Double,
        @SerialName("current_amount") val currentAmount: Double,
        val icon: String?,
        val color: String?,
        @SerialName("target_date")    val targetDate: String?,
        @SerialName("is_completed")   val isCompleted: Boolean,
        @SerialName("is_deleted")    val isDeleted: Boolean = false
    )

    @Serializable
    private data class BudgetUpsertDto(
        val id: String? = null,
        @SerialName("user_id")     val userId: String,
        @SerialName("category_id")   val categoryId: String,
        @SerialName("amount_limit")  val amountLimit: Double,
        val month: Int,
        val year: Int,
        @SerialName("is_deleted")    val isDeleted: Boolean = false
    )

    @Serializable
    private data class TransactionUpsertDto(
        val id: String? = null,
        @SerialName("user_id")          val userId: String,
        @SerialName("wallet_id")        val walletId: String,
        @SerialName("category_id")      val categoryId: String,
        val note: String,
        val amount: Double,
        val type: String,
        @SerialName("transaction_date") val transactionDate: String,
        @SerialName("ai_generated")     val aiGenerated: Boolean,
        @SerialName("is_deleted")       val isDeleted: Boolean = false,
        @SerialName("image_path")       val imagePath: String? = null
    )

    @Serializable
    private data class SavingHistoryUpsertDto(
        val id: String? = null,
        @SerialName("saving_id")        val savingId: String,
        val amount: Double,
        val note: String?,
        @SerialName("transaction_date") val transactionDate: String
    )

    @Serializable
    private data class RemoteIdDto(val id: String, val name: String? = null)

    @Serializable
    private data class CategoryRemoteDto(
        val id: String,
        @SerialName("user_id") val userId: String,
        val name: String,
        val type: String,
        val icon: String,
        val color: String,
        @SerialName("is_system") val isSystem: Boolean,
        @SerialName("is_deleted") val isDeleted: Boolean
    )

    @Serializable
    private data class WalletRemoteDto(
        val id: String,
        @SerialName("user_id") val userId: String,
        val name: String,
        val balance: Double,
        val icon: String,
        val color: String,
        @SerialName("is_default") val isDefault: Boolean,
        @SerialName("is_deleted") val isDeleted: Boolean
    )

    @Serializable
    private data class SavingRemoteDto(
        val id: String,
        @SerialName("user_id") val userId: String,
        val name: String,
        @SerialName("target_amount") val targetAmount: Double,
        @SerialName("current_amount") val currentAmount: Double,
        val icon: String?,
        val color: String?,
        @SerialName("target_date") val targetDate: String?,
        @SerialName("is_completed") val isCompleted: Boolean,
        @SerialName("is_deleted") val isDeleted: Boolean
    )

    @Serializable
    private data class BudgetRemoteDto(
        val id: String,
        @SerialName("user_id") val userId: String,
        @SerialName("category_id") val categoryId: String,
        @SerialName("amount_limit") val amountLimit: Double,
        val month: Int,
        val year: Int,
        @SerialName("is_deleted") val isDeleted: Boolean
    )

    @Serializable
    private data class TransactionRemoteDto(
        val id: String,
        @SerialName("user_id") val userId: String,
        @SerialName("wallet_id") val walletId: String,
        @SerialName("category_id") val categoryId: String,
        val note: String,
        val amount: Double,
        val type: String,
        @SerialName("transaction_date") val transactionDate: String,
        @SerialName("ai_generated") val aiGenerated: Boolean,
        @SerialName("is_deleted") val isDeleted: Boolean,
        @SerialName("image_path") val imagePath: String? = null
    )

    @Serializable
    private data class SavingHistoryRemoteDto(
        val id: String,
        @SerialName("saving_id") val savingId: String,
        val amount: Double,
        val note: String?,
        @SerialName("transaction_date") val transactionDate: String
    )

    // ── Public API ────────────────────────────────────────────────────────────

    suspend fun syncAll(userId: String, username: String): Boolean {
        return try {
            SupabaseClient.client.auth.retrieveUserForCurrentSession(updateSession = true)

            // Kiểm tra dữ liệu local
            val isEmpty = isLocalDataEmpty(userId)
            Log.d(TAG, "Local data empty for $userId: $isEmpty")

            if (isEmpty) {
                // Trường hợp 1: Local chưa có gì -> Kéo từ Supabase về
                pullFromSupabase(userId, username)
            } else {
                // Trường hợp 2: Local có dữ liệu -> Đẩy lên Supabase + Dọn dẹp
                syncProfile(userId, username)
                val categoryMap = syncCategories(userId)
                val walletMap = syncWallets(userId)
                val savingMap = syncSavings(userId)
                syncBudgets(userId, categoryMap)
                syncTransactions(userId, walletMap, categoryMap)
                syncSavingRecords(savingMap)

                // Dọn dẹp dữ liệu thừa trên Supabase
                cleanupRemoteData(userId)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            false
        }
    }

    private suspend fun isLocalDataEmpty(userId: String): Boolean {
        val walletCount = db.walletDao().getWalletsByUser(userId).size
        val categoryCount = db.categoryDao().countSystemCategories(userId) + 
                          (db.categoryDao().observeCategories(userId).first().size)
        // Nếu không có ví nào và không có category nào (hoặc chỉ có system default mà chưa sync)
        return walletCount == 0 && categoryCount <= 0
    }

    private suspend fun pullFromSupabase(userId: String, username: String) {
        Log.d(TAG, "Pulling data from Supabase for $userId")
        syncProfile(userId, username)

        // 1. Categories
        val remoteCategories = SupabaseClient.client.postgrest["categories"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<CategoryRemoteDto>()
        
        val categoryMap = mutableMapOf<String, Long>() // Supabase ID -> Local ID
        remoteCategories.forEach { remote ->
            val entity = com.example.mymoney.data.local.entity.CategoryEntity(
                supabaseId = remote.id,
                userId = userId,
                name = remote.name,
                type = remote.type,
                icon = remote.icon,
                color = remote.color,
                isSystem = remote.isSystem,
                isDeleted = remote.isDeleted,
                syncStatus = com.example.mymoney.data.local.entity.SyncStatus.SYNCED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val localId = db.categoryDao().insert(entity)
            categoryMap[remote.id] = localId
        }

        // 2. Wallets
        val remoteWallets = SupabaseClient.client.postgrest["wallets"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<WalletRemoteDto>()

        val walletMap = mutableMapOf<String, Long>() // Supabase ID -> Local ID
        remoteWallets.forEach { remote ->
            val entity = com.example.mymoney.data.local.entity.WalletEntity(
                supabaseId = remote.id,
                userId = userId,
                name = remote.name,
                balance = remote.balance,
                icon = remote.icon,
                color = remote.color,
                isDefault = remote.isDefault,
                isDeleted = remote.isDeleted,
                syncStatus = com.example.mymoney.data.local.entity.SyncStatus.SYNCED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val localId = db.walletDao().insert(entity)
            walletMap[remote.id] = localId
        }

        // 3. Savings
        val remoteSavings = SupabaseClient.client.postgrest["savings"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<SavingRemoteDto>()

        val savingMap = mutableMapOf<String, Long>() // Supabase ID -> Local ID
        remoteSavings.forEach { remote ->
            val entity = com.example.mymoney.data.local.entity.SavingGoalEntity(
                supabaseId = remote.id,
                userId = userId,
                name = remote.name,
                targetAmount = remote.targetAmount,
                currentAmount = remote.currentAmount,
                icon = remote.icon,
                color = remote.color,
                targetDate = remote.targetDate?.let { isoToMillis(it) },
                isCompleted = remote.isCompleted,
                isDeleted = remote.isDeleted,
                syncStatus = com.example.mymoney.data.local.entity.SyncStatus.SYNCED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val localId = db.savingDao().insert(entity)
            savingMap[remote.id] = localId
        }

        // 4. Budgets
        val remoteBudgets = SupabaseClient.client.postgrest["budgets"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<BudgetRemoteDto>()

        remoteBudgets.forEach { remote ->
            val localCatId = categoryMap[remote.categoryId] ?: return@forEach
            val entity = com.example.mymoney.data.local.entity.BudgetEntity(
                supabaseId = remote.id,
                userId = userId,
                categoryId = localCatId,
                amountLimit = remote.amountLimit,
                month = remote.month,
                year = remote.year,
                isDeleted = remote.isDeleted,
                syncStatus = com.example.mymoney.data.local.entity.SyncStatus.SYNCED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.budgetDao().insert(entity)
        }

        // 5. Transactions
        val remoteTransactions = SupabaseClient.client.postgrest["transactions"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<TransactionRemoteDto>()

        remoteTransactions.forEach { remote ->
            val localWalletId = walletMap[remote.walletId] ?: return@forEach
            val localCatId = categoryMap[remote.categoryId]
            val catName = remoteCategories.find { it.id == remote.categoryId }?.name ?: "Khác"

            val entity = com.example.mymoney.data.local.entity.TransactionEntity(
                supabaseId = remote.id,
                userId = userId,
                walletId = localWalletId,
                categoryId = localCatId,
                categoryName = catName,
                amount = remote.amount,
                type = remote.type,
                note = remote.note,
                transactionDate = isoToMillis(remote.transactionDate),
                aiGenerated = remote.aiGenerated,
                isDeleted = remote.isDeleted,
                syncStatus = com.example.mymoney.data.local.entity.SyncStatus.SYNCED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                imagePath = remote.imagePath
            )
            db.transactionDao().insert(entity)
        }

        // 6. Saving Records
        remoteSavings.forEach { remoteSaving ->
            val localSavingId = savingMap[remoteSaving.id] ?: return@forEach
            val remoteRecords = SupabaseClient.client.postgrest["saving_history"]
                .select { filter { eq("saving_id", remoteSaving.id) } }
                .decodeList<SavingHistoryRemoteDto>()
            
            remoteRecords.forEach { remoteRec ->
                val entity = com.example.mymoney.data.local.entity.SavingRecordEntity(
                    supabaseId = remoteRec.id,
                    userId = userId,
                    savingGoalId = localSavingId,
                    amount = remoteRec.amount,
                    note = remoteRec.note,
                    recordDate = isoToMillis(remoteRec.transactionDate),
                    syncStatus = com.example.mymoney.data.local.entity.SyncStatus.SYNCED,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                db.savingRecordDao().insertRecord(entity)
            }
        }
        Log.d(TAG, "Pull completed successfully")
    }

    private suspend fun cleanupRemoteData(
        userId: String
    ) {
        try {
            Log.d(TAG, "Cleanup finished (Sync handles deletion via is_deleted flag)")
        } catch (e: Exception) {
            Log.w(TAG, "Cleanup failed: ${e.message}")
        }
    }

    private suspend fun syncProfile(userId: String, username: String) {
        try {
            SupabaseClient.client.postgrest["profiles"].upsert(
                buildJsonObject {
                    put("id", userId)
                    put("username", username)
                }
            )
            Log.d(TAG, "Profile synced: $username")
        } catch (e: Exception) {
            Log.w(TAG, "Profile sync failed: ${e.message}")
        }
    }

    private suspend fun syncCategories(userId: String): Map<String, String> {
        val dao = db.categoryDao()
        val pending = dao.getPendingSync(userId)
        
        pending.forEach { cat ->
            val dto = CategoryUpsertDto(
                id = cat.supabaseId,
                userId = userId,
                name = cat.name,
                type = cat.type,
                icon = cat.icon,
                color = cat.color,
                isSystem = cat.isSystem,
                isDeleted = cat.isDeleted
            )
            try {
                val response = SupabaseClient.client.postgrest["categories"]
                    .upsert(dto) { select() }
                    .decodeSingle<RemoteIdDto>()
                dao.markSynced(cat.id, response.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync category ${cat.name}: ${e.message}")
            }
        }

        return SupabaseClient.client.postgrest["categories"]
            .select()
            .decodeList<RemoteIdDto>()
            .associateBy({ it.name ?: "" }, { it.id })
    }

    private suspend fun syncWallets(userId: String): Map<Long, String> {
        val dao = db.walletDao()
        val pending = dao.getPendingSync(userId)

        pending.forEach { wallet ->
            val dto = WalletUpsertDto(
                id = wallet.supabaseId,
                userId = userId,
                name = wallet.name,
                balance = wallet.balance,
                icon = wallet.icon,
                color = wallet.color,
                isDefault = wallet.isDefault,
                isDeleted = wallet.isDeleted
            )
            try {
                val response = SupabaseClient.client.postgrest["wallets"]
                    .upsert(dto) { select() }
                    .decodeSingle<RemoteIdDto>()
                dao.markSynced(wallet.id, response.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync wallet ${wallet.name}: ${e.message}")
            }
        }

        val allLocal = dao.getWalletsByUser(userId)
        return allLocal.filter { it.supabaseId != null }.associate { it.id to it.supabaseId!! }
    }

    private suspend fun syncSavings(userId: String): Map<Long, String> {
        val dao = db.savingDao()
        val pending = dao.getPendingSync(userId)

        pending.forEach { s ->
            // Thử tìm remote ID nếu local đang null để tránh lỗi RLS/Duplicate khi upsert
            var remoteId = s.supabaseId
            if (remoteId == null) {
                try {
                    val existing = SupabaseClient.client.postgrest["savings"]
                        .select {
                            filter {
                                eq("user_id", userId)
                                eq("name", s.name)
                            }
                        }
                        .decodeList<RemoteIdDto>()
                        .firstOrNull()
                    remoteId = existing?.id
                    if (remoteId != null) {
                        Log.d(TAG, "Found existing remote saving ID for ${s.name}: $remoteId")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Check existing saving failed: ${e.message}")
                }
            }

            val dto = SavingUpsertDto(
                id = remoteId,
                userId = userId,
                name = s.name,
                targetAmount = s.targetAmount,
                currentAmount = s.currentAmount,
                icon = s.icon,
                color = s.color,
                targetDate = s.targetDate?.let { millisToIso(it) },
                isCompleted = s.isCompleted,
                isDeleted = s.isDeleted
            )
            try {
                val response = SupabaseClient.client.postgrest["savings"]
                    .upsert(dto) { select() }
                    .decodeSingle<RemoteIdDto>()
                dao.markSynced(s.id, response.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync saving ${s.name}: ${e.message}")
            }
        }

        val allLocal = db.savingDao().observeSavingGoals(userId).first()
        return allLocal.filter { it.supabaseId != null }.associate { it.id to it.supabaseId!! }
    }

    private suspend fun syncBudgets(userId: String, categoryMap: Map<String, String>) {
        val dao = db.budgetDao()
        val pending = dao.getPendingSync(userId)

        pending.forEach { b ->
            val category = db.categoryDao().getCategoryById(b.categoryId) ?: return@forEach
            val catSupabaseId = categoryMap[category.name] ?: return@forEach

            // Thử tìm remote ID nếu local đang null để tránh lỗi Unique Constraint
            var remoteId = b.supabaseId
            if (remoteId == null) {
                try {
                    val existing = SupabaseClient.client.postgrest["budgets"]
                        .select {
                            filter {
                                eq("user_id", userId)
                                eq("category_id", catSupabaseId)
                                eq("month", b.month)
                                eq("year", b.year)
                            }
                        }
                        .decodeList<RemoteIdDto>()
                        .firstOrNull()
                    remoteId = existing?.id
                    if (remoteId != null) {
                        Log.d(TAG, "Found existing remote budget ID: $remoteId")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Check existing budget failed: ${e.message}")
                }
            }

            val dto = BudgetUpsertDto(
                id = remoteId,
                userId = userId,
                categoryId = catSupabaseId,
                amountLimit = b.amountLimit,
                month = b.month,
                year = b.year,
                isDeleted = b.isDeleted
            )
            try {
                val response = SupabaseClient.client.postgrest["budgets"]
                    .upsert(dto) { select() }
                    .decodeSingle<RemoteIdDto>()
                dao.markSynced(b.id, response.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync budget for cat ${category.name}: ${e.message}")
            }
        }
    }

    private suspend fun syncTransactions(
        userId: String,
        walletMap: Map<Long, String>,
        categoryMap: Map<String, String>
    ) {
        val dao = db.transactionDao()
        val pending = dao.getPendingSync(userId)

        pending.forEach { tx ->
            val walletSupabaseId = walletMap[tx.walletId] ?: return@forEach
            val catSupabaseId = categoryMap[tx.categoryName] ?: categoryMap["Khác"] ?: return@forEach

            val dto = TransactionUpsertDto(
                id = tx.supabaseId,
                userId = userId,
                walletId = walletSupabaseId,
                categoryId = catSupabaseId,
                note = tx.note,
                amount = tx.amount,
                type = tx.type,
                transactionDate = millisToIso(tx.transactionDate),
                aiGenerated = tx.aiGenerated,
                isDeleted = tx.isDeleted,
                imagePath = tx.imagePath
            )
            try {
                val response = SupabaseClient.client.postgrest["transactions"]
                    .upsert(dto) { select() }
                    .decodeSingle<RemoteIdDto>()
                dao.markSynced(tx.id, response.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync transaction ${tx.note}: ${e.message}")
            }
        }
    }

    private suspend fun syncSavingRecords(savingMap: Map<Long, String>) {
        val dao = db.savingRecordDao()
        val pending = dao.getPendingSync()

        pending.forEach { r ->
            val savingSupabaseId = savingMap[r.savingGoalId] ?: return@forEach
            val dto = SavingHistoryUpsertDto(
                id = r.supabaseId,
                savingId = savingSupabaseId,
                amount = r.amount,
                note = r.note,
                transactionDate = millisToIso(r.recordDate)
            )
            try {
                val response = SupabaseClient.client.postgrest["saving_history"]
                    .upsert(dto) { select() }
                    .decodeSingle<RemoteIdDto>()
                dao.markSynced(r.id, response.id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync saving record: ${e.message}")
            }
        }
    }

    private fun millisToIso(millis: Long): String = Instant.ofEpochMilli(millis).toString()

    private fun isoToMillis(iso: String): Long = try {
        Instant.parse(iso).toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
