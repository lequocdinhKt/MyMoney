package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mymoney.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Query("SELECT * FROM recurring_transactions WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeAll(userId: String): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE user_id = :userId AND is_active = 1 AND next_due_date <= :now")
    suspend fun getDueRecurring(userId: String, now: Long = System.currentTimeMillis()): List<RecurringTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecurringTransactionEntity): Long

    @Update
    suspend fun update(entity: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET is_active = :isActive WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean)

    @Query("UPDATE recurring_transactions SET next_due_date = :nextDueDate WHERE id = :id")
    suspend fun updateNextDueDate(id: Long, nextDueDate: Long)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun delete(id: Long)
}

