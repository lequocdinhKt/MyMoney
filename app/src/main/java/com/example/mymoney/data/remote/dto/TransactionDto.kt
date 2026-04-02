package com.example.mymoney.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO để insert/select giao dịch trên Supabase (bảng "transactions").
 * Tên cột khớp với schema Supabase.
 */
@Serializable
data class TransactionDto(
    val id: String? = null,                 // UUID, null khi insert (Supabase tự gen)
    @SerialName("user_id")
    val userId: String,
    val note: String,
    val amount: Double,
    val type: String,                       // "income" | "expense"
    val category: String,
    @SerialName("wallet_id")
    val walletId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null           // ISO 8601, null khi insert
)
