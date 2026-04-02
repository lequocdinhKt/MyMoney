package com.example.mymoney.data.repository

import android.util.Log
import com.example.mymoney.data.remote.SupabaseClient
import com.example.mymoney.data.remote.dto.TransactionDto
import com.example.mymoney.domain.model.TransactionModel
import io.github.jan.supabase.postgrest.postgrest

/**
 * Lưu giao dịch lên Supabase (bảng "transactions").
 * Tách riêng khỏi [TransactionRepositoryImpl] (Room) để rõ ràng.
 */
class SupabaseTransactionRepository {
    private val TAG = "SupabaseTxRepo"

    /**
     * Insert giao dịch lên Supabase.
     * @param transaction Domain model
     * @param userId      UUID của user từ Supabase Auth
     * @return UUID được Supabase tạo ra, null nếu lỗi
     */
    suspend fun insertTransaction(
        transaction: TransactionModel,
        userId: String
    ): String? = try {
        val dto = TransactionDto(
            userId   = userId,
            note     = transaction.note,
            amount   = transaction.amount,
            type     = transaction.type,
            category = transaction.category
        )
        val result = SupabaseClient.client.postgrest["transactions"]
            .insert(dto) { select() }
            .decodeSingleOrNull<TransactionDto>()
        Log.d(TAG, "Inserted to Supabase: ${result?.id}")
        result?.id
    } catch (e: Exception) {
        Log.e(TAG, "Failed to insert to Supabase: ${e.message}", e)
        null  // không crash app nếu Supabase lỗi
    }
}
