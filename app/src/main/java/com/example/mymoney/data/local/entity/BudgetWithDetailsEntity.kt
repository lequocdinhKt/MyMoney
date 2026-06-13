package com.example.mymoney.data.local.entity

import androidx.room.ColumnInfo

data class BudgetWithDetailsEntity(
    val id: Long,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "amount_limit") val amountLimit: Double,
    val month: Int,
    val year: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "supabase_id") val supabaseId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "category_icon") val categoryIcon: String,
    @ColumnInfo(name = "spent_amount") val spentAmount: Double,
)
