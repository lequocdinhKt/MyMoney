package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.CategoryDao
import com.example.mymoney.data.local.entity.CategoryEntity
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.domain.model.CategoryModel
import com.example.mymoney.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategories(userId: String): Flow<List<CategoryModel>> =
        categoryDao.observeCategories(userId).map { list -> list.map { it.toModel() } }

    override fun getCategoriesByType(userId: String, type: String): Flow<List<CategoryModel>> =
        categoryDao.observeCategoriesByType(userId, type).map { list -> list.map { it.toModel() } }

    override suspend fun addCategory(category: CategoryModel): Long =
        categoryDao.insert(category.toEntity())

    override suspend fun updateCategory(category: CategoryModel) =
        categoryDao.update(category.toEntity().copy(syncStatus = SyncStatus.PENDING_UPDATE))

    override suspend fun deleteCategory(id: Long) =
        categoryDao.softDelete(id)

    override suspend fun seedDefaultCategories(userId: String) {
        // Idempotent guard: nếu đã có system-category thì skip.
        // An toàn khi gọi lại sau cài đặt lại app hoặc login trên thiết bị mới.
        if (categoryDao.countSystemCategories(userId) > 0) return

        val now = System.currentTimeMillis()
        data class Seed(val name: String, val type: String, val icon: String, val color: String)
        val seeds = listOf(
            Seed("Ăn uống",   "expense", "🍜", "#FF6B6B"),
            Seed("Di chuyển", "expense", "🚗", "#4ECDC4"),
            Seed("Mua sắm",   "expense", "🛍", "#45B7D1"),
            Seed("Nhà cửa",   "expense", "🏠", "#FF9800"),
            Seed("Giải trí",  "expense", "🎮", "#96CEB4"),
            Seed("Sức khỏe",  "expense", "💊", "#FFEAA7"),
            Seed("Giáo dục",  "expense", "📚", "#DDA0DD"),
            Seed("Hóa đơn",   "expense", "📄", "#98D8C8"),
            Seed("Khác",      "expense", "📦", "#B0B0B0"),
            Seed("Thu nhập",  "income",  "💰", "#55EFC4"),
            Seed("Thưởng",    "income",  "🎁", "#FDCB6E"),
            Seed("Đầu tư",    "income",  "📈", "#6C5CE7"),
            Seed("Khác",      "income",  "💼", "#A29BFE")
        )
        categoryDao.insertAll(seeds.map { s ->
            CategoryEntity(
                userId     = userId,
                name       = s.name,
                type       = s.type,
                icon       = s.icon,
                color      = s.color,
                isSystem   = true,
                createdAt  = now,
                updatedAt  = now,
                syncStatus = SyncStatus.PENDING_INSERT
            )
        })
    }

    // ── Mappers ──

    private fun CategoryEntity.toModel() = CategoryModel(
        id         = id,
        userId     = userId,
        name       = name,
        type       = type,
        icon       = icon,
        color      = color,
        isSystem   = isSystem,
        isArchived = isDeleted,
        createdAt  = createdAt,
        supabaseId = supabaseId
    )

    private fun CategoryModel.toEntity(): CategoryEntity {
        val now = System.currentTimeMillis()
        return CategoryEntity(
            id         = id,
            supabaseId = supabaseId,
            userId     = userId ?: "",
            name       = name,
            type       = type,
            icon       = icon,
            color      = color,
            isSystem   = isSystem,
            createdAt  = createdAt,
            updatedAt  = now,
            isDeleted  = isArchived,
            syncStatus = SyncStatus.PENDING_INSERT
        )
    }
}


