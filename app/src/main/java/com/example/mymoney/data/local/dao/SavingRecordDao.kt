package com.example.mymoney.data.local.dao

import androidx.room.*
import com.example.mymoney.data.local.entity.SavingRecordEntity
import com.example.mymoney.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingRecordDao {
    @Query("SELECT * FROM saving_records WHERE saving_goal_id = :goalId AND is_deleted = 0 ORDER BY record_date DESC")
    fun getRecordsByGoalId(goalId: Long): Flow<List<SavingRecordEntity>>

    @Query("SELECT * FROM saving_records WHERE id = :id AND is_deleted = 0")
    suspend fun getRecordById(id: Long): SavingRecordEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecord(record: SavingRecordEntity): Long

    @Update
    suspend fun updateRecord(record: SavingRecordEntity)

    @Query("UPDATE saving_records SET is_deleted = 1, sync_status = ${SyncStatus.PENDING_DELETE}, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM saving_records WHERE sync_status != ${SyncStatus.SYNCED}")
    suspend fun getPendingSync(): List<SavingRecordEntity>

    @Query(" UPDATE saving_records SET sync_status = ${SyncStatus.SYNCED}, supabase_id = :supabaseId WHERE id = :localId")
    suspend fun markSynced(localId: Long, supabaseId: String)

    @Query("DELETE FROM saving_records WHERE user_id = :userId AND is_deleted = 1")
    suspend fun hardDeleteDeletedItems(userId: String)
}
