package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mymoney.data.local.entity.CategoryEntity
import com.example.mymoney.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE user_id = :userId AND is_deleted = 0 ORDER BY name ASC")
    fun observeCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE user_id = :userId AND type = :type AND is_deleted = 0 ORDER BY name ASC")
    fun observeCategoriesByType(userId: String, type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND is_deleted = 0")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    /** Tìm category theo tên và type — dùng để resolve AI category name → id */
    @Query("SELECT * FROM categories WHERE user_id = :userId AND name = :name AND type = :type AND is_deleted = 0 LIMIT 1")
    suspend fun getCategoryByName(userId: String, name: String, type: String): CategoryEntity?

    /** Lấy category "Khác" làm fallback khi không resolve được tên */
    @Query("SELECT * FROM categories WHERE user_id = :userId AND name = 'Khác' AND type = :type AND is_deleted = 0 LIMIT 1")
    suspend fun getDefaultCategory(userId: String, type: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    /** Soft-delete — chỉ cho phép nếu không còn giao dịch tham chiếu (RESTRICT được enforce ở FK) */
    @Query("UPDATE categories SET is_deleted = 1, sync_status = ${SyncStatus.PENDING_DELETE}, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    /** Đếm số system-category chưa bị xóa của user — dùng để guard seed */
    @Query("SELECT COUNT(*) FROM categories WHERE user_id = :userId AND is_system = 1 AND is_deleted = 0")
    suspend fun countSystemCategories(userId: String): Int

    @Query("SELECT * FROM categories WHERE user_id = :userId AND sync_status != ${SyncStatus.SYNCED}")
    suspend fun getPendingSync(userId: String): List<CategoryEntity>

    @Query("UPDATE categories SET sync_status = ${SyncStatus.SYNCED}, supabase_id = :supabaseId WHERE id = :localId")
    suspend fun markSynced(localId: Long, supabaseId: String)

    /** Cập nhật supabase_id theo tên category — dùng khi sync từ Supabase xuống */
    @Query("UPDATE categories SET supabase_id = :supabaseId WHERE name = :name AND is_deleted = 0")
    suspend fun updateSupabaseIdByName(name: String, supabaseId: String)
}

