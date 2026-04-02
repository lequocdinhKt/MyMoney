package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymoney.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho bảng "transactions".
 */
@Dao
interface TransactionDao {

    /** Tất cả giao dịch theo thời gian DESC */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /** Giao dịch trong khoảng thời gian [from, to] */
    @Query("SELECT * FROM transactions WHERE timestamp >= :from AND timestamp < :to ORDER BY timestamp DESC")
    fun getTransactionsByPeriod(from: Long, to: Long): Flow<List<TransactionEntity>>

    /** Tổng thu nhập trong khoảng thời gian */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'income' AND timestamp >= :from AND timestamp < :to")
    fun getTotalIncome(from: Long, to: Long): Flow<Double>

    /** Tổng chi tiêu trong khoảng thời gian */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'expense' AND timestamp >= :from AND timestamp < :to")
    fun getTotalExpense(from: Long, to: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}
