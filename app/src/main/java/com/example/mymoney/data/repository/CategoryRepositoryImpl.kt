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
        val now = System.currentTimeMillis()
        val defaults = listOf(
            // Expense
            triple("Ăn uống",    "expense", "🍜", "#FF6B6B"),
            triple("Di chuyển",  "expense", "🚗", "#4ECDC4"),
            triple("Mua sắm",    "expense", "🛍", "#45B7D1"),
            triple("Giải trí",   "expense", "🎮", "#96CEB4"),
            triple("Sức khỏe",   "expense", "💊", "#FFEAA7"),
            triple("Giáo dục",   "expense", "📚", "#DDA0DD"),
            triple("Hóa đơn",    "expense", "📄", "#98D8C8"),
            triple("Khác",       "expense", "📦", "#B0B0B0"),
            // Income
            triple("Thu nhập",   "income",  "💰", "#55EFC4"),
            triple("Thưởng",     "income",  "🎁", "#FDCB6E"),
            triple("Đầu tư",     "income",  "📈", "#6C5CE7"),
            triple("Khác",       "income",  "💼", "#A29BFE")
        ).map { (name, type, icon, color) ->
            CategoryEntity(
                userId     = userId,
                name       = name,
                type       = type,
                icon       = icon,
                color      = color,
                isSystem   = true,
                createdAt  = now,
                updatedAt  = now,
                syncStatus = SyncStatus.PENDING_INSERT
            )
        }
        categoryDao.insertAll(defaults)
    }

    private fun triple(a: String, b: String, c: String, d: String) = listOf(a, b, c, d)
        .let { (name, type, icon, color) -> Triple(Triple(name, type, icon), color, Unit) }
        .let { (inner, color, _) -> Triple(inner.first, inner.second, inner.third) to color }
        .let { (t, color) -> listOf(t.first, t.second, t.third, color) }
        .let { it[0] to it[1] to it[2] to it[3] }

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

