package com.example.mymoney.domain.model

/**
 * Domain model cho giao dịch định kỳ.
 * Pure Kotlin – không phụ thuộc Android hay Room.
 */
data class RecurringTransactionModel(
    val id: Long = 0L,
    val userId: String = "",
    val walletId: Long = 0L,
    val categoryName: String = "Khác",
    val amount: Double,
    /** "income" | "expense" */
    val type: String = "expense",
    val note: String,
    /** "daily" | "weekly" | "monthly" | "yearly" */
    val frequency: String = "monthly",
    val startDate: Long = System.currentTimeMillis(),
    val nextDueDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

