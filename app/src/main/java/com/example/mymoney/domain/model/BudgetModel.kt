package com.example.mymoney.domain.model

data class BudgetModel(
    val id: Long = 0L,
    val userId: String,
    val categoryId: Long,
    val categoryName: String = "",          // denormalized display field
    val amountLimit: Double,
    val month: Int,
    val year: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val supabaseId: String? = null
)

