package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.data.local.entity.TransactionEntity
import com.example.mymoney.data.local.entity.TransactionWithCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Transaction
    @Query(
        """SELECT * FROM transactions 
          WHERE user_id = :userId
          AND is_deleted = 0
    ORDER BY transaction_date DESC"""
    )
    fun observeTransactionsWithCategory(userId: String): Flow<List<TransactionWithCategoryEntity>>

    @Transaction
    @Query(
        """SELECT * FROM transactions 
           WHERE user_id = :userId AND wallet_id = :walletId
             AND transaction_date BETWEEN :startMs AND :endMs 
             AND is_deleted = 0 
           ORDER BY transaction_date DESC"""
    )
    fun observeByWalletAndDateRangeWithCategory(userId: String, walletId: Long, startMs: Long, endMs: Long): Flow<List<TransactionWithCategoryEntity>>

    @Query(
        """SELECT * FROM transactions 
           WHERE user_id = :userId AND wallet_id = :walletId
             AND transaction_date BETWEEN :startMs AND :endMs 
             AND is_deleted = 0 
           ORDER BY transaction_date DESC"""
    )
    fun observeByWalletAndDateRange(userId: String, walletId: Long, startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Query(
        """SELECT * FROM transactions 
           WHERE user_id = :userId 
             AND transaction_date BETWEEN :startMs AND :endMs 
             AND is_deleted = 0 
           ORDER BY transaction_date DESC"""
    )
    fun observeByDateRange(userId: String, startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    @Transaction
    @Query(
        """SELECT * FROM transactions 
           WHERE user_id = :userId 
             AND transaction_date BETWEEN :startMs AND :endMs 
             AND is_deleted = 0 
           ORDER BY transaction_date DESC"""
    )
    fun observeByDateRangeWithCategory(userId: String, startMs: Long, endMs: Long): Flow<List<TransactionWithCategoryEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id AND is_deleted = 0")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query(
        """SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
           WHERE user_id = :userId AND type = 'income' 
             AND transaction_date BETWEEN :startMs AND :endMs 
             AND is_deleted = 0"""
    )
    suspend fun sumIncome(userId: String, startMs: Long, endMs: Long): Double

    @Query(
        """SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
           WHERE user_id = :userId AND type = 'expense' 
             AND transaction_date BETWEEN :startMs AND :endMs 
             AND is_deleted = 0"""
    )
    suspend fun sumExpense(userId: String, startMs: Long, endMs: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transactions SET is_deleted = 1, sync_status = ${SyncStatus.PENDING_DELETE}, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND sync_status != ${SyncStatus.SYNCED}")
    suspend fun getPendingSync(userId: String): List<TransactionEntity>

    @Query("UPDATE transactions SET sync_status = ${SyncStatus.SYNCED}, supabase_id = :supabaseId WHERE id = :localId")
    suspend fun markSynced(localId: Long, supabaseId: String)

    @Query("DELETE FROM transactions WHERE user_id = :userId AND is_deleted = 1")
    suspend fun hardDeleteDeletedItems(userId: String)

    @Query("UPDATE transactions SET image_path = :imagePath, updated_at = :now WHERE id = :id")
    suspend fun updateImagePath(id: Long, imagePath: String, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND image_path IS NOT NULL AND is_deleted = 0 ORDER BY transaction_date DESC")
    fun observePhotoTransactions(userId: String): Flow<List<TransactionEntity>>
}
