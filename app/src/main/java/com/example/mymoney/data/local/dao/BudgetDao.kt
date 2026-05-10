package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mymoney.data.local.entity.BudgetEntity
import com.example.mymoney.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE user_id = :userId AND month = :month AND year = :year AND is_deleted = 0")
    fun observeBudgets(userId: String, month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE user_id = :userId AND category_id = :categoryId AND month = :month AND year = :year AND is_deleted = 0 LIMIT 1")
    suspend fun getBudget(userId: String, categoryId: Long, month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("UPDATE budgets SET is_deleted = 1, sync_status = ${SyncStatus.PENDING_DELETE}, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM budgets WHERE user_id = :userId AND sync_status != ${SyncStatus.SYNCED}")
    suspend fun getPendingSync(userId: String): List<BudgetEntity>

    @Query("UPDATE budgets SET sync_status = ${SyncStatus.SYNCED}, supabase_id = :supabaseId WHERE id = :localId")
    suspend fun markSynced(localId: Long, supabaseId: String)
}

