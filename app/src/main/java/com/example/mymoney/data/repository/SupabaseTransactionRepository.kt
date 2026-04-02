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
        @SerialName("created_at") val createdAt: String   // ISO 8601
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
            userId    = userId,
            note      = note,
            amount    = amount,
            type      = type,
            category  = category,
            createdAt = millisToIso(timestampMillis)
        )
        SupabaseClient.client.postgrest["transactions"].insert(dto)
        Log.d(TAG, "Inserted 1 transaction to Supabase: $note $amount")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Insert failed: ${e.message}", e)
        false
    }

    /**
     * Batch insert toàn bộ danh sách giao dịch từ Room lên Supabase (dùng cho backup).
     * Supabase RLS policy tự xử lý duplicate nếu có unique constraint.
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
            // Batch insert toàn bộ trong 1 request
            SupabaseClient.client.postgrest["transactions"].insert(dtos)
            Log.d(TAG, "Batch inserted ${dtos.size} transactions to Supabase")
            dtos.size
        } catch (e: Exception) {
            Log.e(TAG, "Batch insert failed, trying one-by-one: ${e.message}")
            // Fallback: insert từng cái, bỏ qua lỗi individual (duplicate, v.v.)
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

    /** epoch millis → ISO 8601 string (ví dụ: "2026-04-02T07:00:00Z") */
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
