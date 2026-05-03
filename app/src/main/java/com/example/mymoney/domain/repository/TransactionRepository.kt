package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.TransactionModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface cho giao dịch — thuộc Domain layer.
 * Data layer sẽ implement interface này (dependency inversion).
 *
 * Quy tắc:
 *   - KHÔNG import Room, Android SDK, hay bất kỳ thư viện cụ thể nào.
 *   - Chỉ dùng Kotlin + Coroutines.
 */
interface TransactionRepository {

    /**
     * Lấy tất cả giao dịch của [userId], sắp xếp theo thời gian mới nhất trước.
     * userId truyền qua tham số để Repository có thể là Singleton – không cần
     * khởi tạo lại khi đổi tài khoản.
     */
    fun getAllTransactions(userId: String): Flow<List<TransactionModel>>

    /**
     * Lấy giao dịch của [userId] trong khoảng thời gian [from, to) (mili giây).
     */
    fun getTransactionsByPeriod(userId: String, from: Long, to: Long): Flow<List<TransactionModel>>

    /** Lấy giao dịch của [userId] theo [walletId] trong khoảng thời gian [from, to). */
    fun getTransactionsByWalletAndPeriod(userId: String, walletId: Long, from: Long, to: Long): Flow<List<TransactionModel>>

    fun getTotalIncome(userId: String, from: Long, to: Long): Flow<Double>
    fun getTotalExpense(userId: String, from: Long, to: Long): Flow<Double>

    fun getTotalIncomeByWallet(userId: String, walletId: Long, from: Long, to: Long): Flow<Double>
    fun getTotalExpenseByWallet(userId: String, walletId: Long, from: Long, to: Long): Flow<Double>

    /**
     * Thêm một giao dịch mới (userId lấy từ TransactionModel.userId).
     */
    suspend fun addTransaction(transaction: TransactionModel)

    /**
     * Xoá một giao dịch theo id (soft-delete).
     */
    suspend fun deleteTransaction(id: Long)
}
