package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymoney.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    /** Lấy tất cả danh mục: hệ thống + của user theo type */
    @Query("""
        SELECT * FROM categories 
        WHERE (userId IS NULL OR userId = :userId)
        AND isArchived = 0
        AND (type = :type OR type = 'both')
        ORDER BY isSystem DESC, sortOrder ASC, name ASC
    """)
    fun getCategoriesByType(userId: String, type: String): Flow<List<CategoryEntity>>

    /** Lấy tất cả danh mục (cả chi tiêu lẫn thu nhập) */
    @Query("""
        SELECT * FROM categories 
        WHERE (userId IS NULL OR userId = :userId) AND isArchived = 0
        ORDER BY isSystem DESC, sortOrder ASC, name ASC
    """)
    fun getAllCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(entity: CategoryEntity): Long

    @Query("DELETE FROM categories WHERE id = :id AND isSystem = 0")
    suspend fun deleteCategoryById(id: Long)

    /** Đếm số danh mục để biết đã seed chưa */
    @Query("SELECT COUNT(*) FROM categories WHERE isSystem = 1")
    suspend fun countSystemCategories(): Int

    /** Lấy supabaseId theo name (để map khi backup lên Supabase) */
    @Query("SELECT supabaseId FROM categories WHERE name = :name LIMIT 1")
    suspend fun getSupabaseIdByName(name: String): String?

    /** Update supabaseId cho category theo name */
    @Query("UPDATE categories SET supabaseId = :supabaseId WHERE name = :name")
    suspend fun updateSupabaseIdByName(name: String, supabaseId: String)

    /** Lấy tất cả system categories (để sync supabaseId từ Supabase) */
    @Query("SELECT * FROM categories WHERE isSystem = 1")
    suspend fun getAllSystemCategories(): List<CategoryEntity>
}
