package com.example.mymoney.data.local.dao

import androidx.room.*
import com.example.mymoney.data.local.entity.SavingGoalEntity
import com.example.mymoney.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingDao {
    @Query("SELECT * FROM saving_goals WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeSavingGoals(userId: String): Flow<List<SavingGoalEntity>>

    @Query("SELECT * FROM saving_goals WHERE id = :id AND is_deleted = 0")
    suspend fun getSavingGoalById(id: Long): SavingGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingGoalEntity): Long

    @Update
    suspend fun update(goal: SavingGoalEntity)

    @Query("UPDATE saving_goals SET is_deleted = 1, sync_status = ${SyncStatus.PENDING_DELETE},updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    @Query(" SELECT * FROM saving_goals WHERE user_id = :userId AND sync_status != ${SyncStatus.SYNCED}")
    suspend fun getPendingSync(userId: String): List<SavingGoalEntity>

    @Query("UPDATE saving_goals SET sync_status = ${SyncStatus.SYNCED}, supabase_id = :supabaseId WHERE id = :localId")
    suspend fun markSynced(localId: Long, supabaseId: String)
}
