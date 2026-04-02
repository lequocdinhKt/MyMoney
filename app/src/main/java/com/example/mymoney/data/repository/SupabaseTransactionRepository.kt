package com.example.mymoney.data.repository

import android.util.Log
import com.example.mymoney.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repository giao tiếp với Supabase cho bảng "transactions".
 * Dùng trong AddTransactionViewModel (insert đơn) và SettingViewModel (backup batch).
 */
class SupabaseTransactionRepository {

    private val TAG = "SupabaseTxRepo"

    @Serializable
    private data class TransactionUpsertDto(
        @SerialName("user_id")    val userId: String,
        val note: String,
        val amount: Double,
        val type: String,
        val category: String,
        @SerialName("created_at") val createdAt: String
    )

    /**
     * Insert 1 giao dịch mới lên Supabase.
     * @return true nếu thành công, false nếu lỗi (non-fatal)
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
            userId    = userId,
            note      = note,
            amount    = amount,
            type      = type,
            category  = category,
            createdAt = millisToIso(timestampMillis)
        )
        SupabaseClient.client.postgrest["transactions"].insert(dto)
        Log.d(TAG, "Inserted 1 transaction: $note $amount")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Insert failed: ${e.message}", e)
        false
    }

    /**
     * Upsert batch toàn bộ danh sách giao dịch từ Room lên Supabase.
     * @return Số lượng đã upload thành công
     */
    suspend fun upsertAll(userId: String, transactions: List<TransactionItem>): Int {
        if (transactions.isEmpty()) return 0
        val dtos = transactions.map { tx ->
            TransactionUpsertDto(
                userId    = userId,
                note      = tx.note,
                amount    = tx.amount,
                type      = tx.type,
                category  = tx.category,
                createdAt = millisToIso(tx.timestampMillis)
            )
        }
        return try {
            SupabaseClient.client.postgrest["transactions"].insert(dtos)
            Log.d(TAG, "Batch inserted ${dtos.size} transactions")
            dtos.size
        } catch (e: Exception) {
            Log.e(TAG, "Batch failed, fallback one-by-one: ${e.message}")
            var ok = 0
            dtos.forEach { dto ->
                try {
                    SupabaseClient.client.postgrest["transactions"].insert(dto)
                    ok++
                } catch (ex: Exception) {
                    Log.w(TAG, "Skip: ${ex.message}")
                }
            }
            ok
        }
    }

    private fun millisToIso(millis: Long): String =
        java.time.Instant.ofEpochMilli(millis).toString()

    /** Lightweight DTO dùng khi backup từ Room */
    data class TransactionItem(
        val note: String,
        val amount: Double,
        val type: String,
        val category: String,
        val timestampMillis: Long
    )
}
