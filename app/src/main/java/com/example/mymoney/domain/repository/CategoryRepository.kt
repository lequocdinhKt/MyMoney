package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.CategoryModel
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(userId: String): Flow<List<CategoryModel>>
    fun getCategoriesByType(userId: String, type: String): Flow<List<CategoryModel>>
    suspend fun addCategory(category: CategoryModel): Long
    suspend fun updateCategory(category: CategoryModel)
    suspend fun deleteCategory(id: Long)
    /** Chèn các danh mục mặc định hệ thống nếu chưa tồn tại */
    suspend fun seedDefaultCategories(userId: String)
}

