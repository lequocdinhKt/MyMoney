package com.example.mymoney.data.repository

import android.util.Log
import com.example.mymoney.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Repository giao tiếp với Supabase cho bảng "transactions".
 * Dùng trong SettingViewModel (backup) và AddTransactionViewModel (insert mới).
 */
class SupabaseTransactionRepository {

    private val TAG = "SupabaseTxRepo"

    @Serializable
    data class TransactionUpsertDto(
        @SerialName("user_id")   val userId: String,
        val note: String,
        val amount: Double,
        val type: String,
        val category: String,
        @SerialName("created_at") val createdAt: String   // ISO 8601
    )

    /**
     * Insert 1 giao dịch mới từ AI chat.
     * @return Supabase UUID của row vừa tạo, null nếu lỗi
     */
    suspend fun insertTransaction(
        userId: String,
        note: String,
        amount: Double,
        type: String,
        category: String,
        timestampMillis: Long
    ): Boolean = try {
        val iso = millisToIso(timestampMillis)
        val dto = TransactionUpsertDto(
            userId    = userId,
            note      = note,
            amount    = amount,
            type      = type,
            category  = category,
            createdAt = iso
        )
        SupabaseClient.client.postgrest["transactions"].insert(dto)
        Log.d(TAG, "Inserted 1 transaction to Supabase")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Insert failed: ${e.message}", e)
        false
    }

    /**
     * Upsert toàn bộ danh sách giao dịch từ Room lên Supabase.
     * Dùng insert batch — Supabase RLS policy tự xử lý duplicate nếu có unique constraint.
     * @return Số lượng giao dịch đã upload thành công
     */
    suspend fun upsertAll(
        userId: String,
        transactions: List<TransactionItem>
    ): Int {
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
            // Insert batch toàn bộ trong 1 request
            SupabaseClient.client.postgrest["transactions"].insert(dtos)
            Log.d(TAG, "Inserted ${dtos.size} transactions to Supabase")
            dtos.size
        } catch (e: Exception) {
            Log.e(TAG, "Batch insert failed, trying one-by-one: ${e.message}")
            // Fallback: insert từng cái một, bỏ qua lỗi individual (duplicate, v.v.)
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

    // Helper: epoch millis → ISO 8601 string
    private fun millisToIso(millis: Long): String {
        val instant = java.time.Instant.ofEpochMilli(millis)
        return instant.toString()   // "2026-04-02T07:00:00Z"
    }

    /** Lightweight model để truyền dữ liệu từ Room vào repository */
    data class TransactionItem(
        val note: String,
        val amount: Double,
        val type: String,
        val category: String,
        val timestampMillis: Long
    )
}
