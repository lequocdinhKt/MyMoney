package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.TransactionModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface cho giao dịch — thuộc Domain layer.
 */
interface TransactionRepository {

    /**
     * Lấy tất cả giao dịch của [userId], sắp xếp theo thời gian mới nhất trước.
     */
    fun getAllTransactions(userId: String): Flow<List<TransactionModel>>

    /**
     * Lấy tất cả giao dịch kèm thông tin category (icon, name).
     */
    fun getAllTransactionsWithCategory(userId: String): Flow<List<TransactionModel>>

    /**
     * Lấy giao dịch của [userId] trong khoảng thời gian [from, to) (mili giây).
     */
    fun getTransactionsByPeriod(userId: String, from: Long, to: Long): Flow<List<TransactionModel>>

    /**
     * Lấy giao dịch kèm category trong khoảng thời gian.
     */
    fun getTransactionsWithCategoryByPeriod(userId: String, from: Long, to: Long): Flow<List<TransactionModel>>

    /** Lấy giao dịch của [userId] theo [walletId] trong khoảng thời gian [from, to). */
    fun getTransactionsByWalletAndPeriod(userId: String, walletId: Long, from: Long, to: Long): Flow<List<TransactionModel>>

    /**
     * Lấy giao dịch kèm category theo ví và thời gian.
     */
    fun getTransactionsWithCategoryByWalletAndPeriod(userId: String, walletId: Long, from: Long, to: Long): Flow<List<TransactionModel>>

    fun getTotalIncome(userId: String, from: Long, to: Long): Flow<Double>
    fun getTotalExpense(userId: String, from: Long, to: Long): Flow<Double>

    fun getTotalIncomeByWallet(userId: String, walletId: Long, from: Long, to: Long): Flow<Double>
    fun getTotalExpenseByWallet(userId: String, walletId: Long, from: Long, to: Long): Flow<Double>

    suspend fun addTransaction(transaction: TransactionModel)
    suspend fun getTransactionById(id: Long): TransactionModel?
    suspend fun deleteTransaction(id: Long)
}
