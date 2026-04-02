package com.example.mymoney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mymoney.domain.model.CategoryModel

/**
 * Room Entity cho bảng "categories".
 * Danh mục giao dịch — system (is_system=true) hoặc do user tự tạo.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
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
) {
    fun toDomain(): CategoryModel = CategoryModel(
        id = id,
        userId = userId,
        name = name,
        icon = icon,
        color = color,
        type = type,
        isSystem = isSystem,
        isArchived = isArchived,
        sortOrder = sortOrder,
        createdAt = createdAt,
        supabaseId = supabaseId
    )

    companion object {
        fun fromDomain(model: CategoryModel): CategoryEntity = CategoryEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            icon = model.icon,
            color = model.color,
            type = model.type,
            isSystem = model.isSystem,
            isArchived = model.isArchived,
            sortOrder = model.sortOrder,
            createdAt = model.createdAt,
            supabaseId = model.supabaseId
        )
    }
}
