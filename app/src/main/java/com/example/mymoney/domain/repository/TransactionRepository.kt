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
     * Lấy tất cả giao dịch, sắp xếp theo thời gian mới nhất trước.
     * Trả về Flow để UI tự động cập nhật khi dữ liệu thay đổi.
     */
    fun getAllTransactions(): Flow<List<TransactionModel>>

    /**
     * Lấy giao dịch trong khoảng thời gian [from, to) (mili giây).
     */
    fun getTransactionsByPeriod(from: Long, to: Long): Flow<List<TransactionModel>>

    /**
     * Lấy tổng thu nhập trong khoảng thời gian [from, to).
     */
    fun getTotalIncome(from: Long, to: Long): Flow<Double>

    /**
     * Lấy tổng chi tiêu trong khoảng thời gian [from, to).
     */
    fun getTotalExpense(from: Long, to: Long): Flow<Double>

    /**
     * Thêm một giao dịch mới.
     * Suspend function — chạy trong coroutine scope.
     */
    suspend fun addTransaction(transaction: TransactionModel)

    /**
     * Xoá một giao dịch theo id.
     */
    suspend fun deleteTransaction(id: Long)
}
