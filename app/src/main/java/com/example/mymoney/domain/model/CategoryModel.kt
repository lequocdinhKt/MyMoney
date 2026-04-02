package com.example.mymoney.domain.model

/**
 * Domain model cho Danh mục giao dịch.
 * Pure Kotlin – không phụ thuộc Android hay Room.
 */
data class CategoryModel(
    val id: Long = 0L,
    val userId: String? = null,         // null = danh mục hệ thống
    val name: String,
    val icon: String,
    val color: String,
    val type: String,                   // "expense", "income", "both"
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val supabaseId: String? = null
)
