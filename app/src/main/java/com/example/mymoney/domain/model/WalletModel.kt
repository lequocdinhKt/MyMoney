package com.example.mymoney.domain.model

/**
 * Domain model cho Ví tiền.
 * Pure Kotlin – không phụ thuộc Android hay Room.
 */
data class WalletModel(
    val id: Long = 0L,
    val userId: String,
    val name: String,
    val balance: Double = 0.0,
    val icon: String = "wallet",
    val color: String = "#0088F0",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val supabaseId: String? = null
)
