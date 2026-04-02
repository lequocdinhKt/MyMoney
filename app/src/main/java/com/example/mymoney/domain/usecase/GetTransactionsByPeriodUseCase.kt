package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case: Lấy danh sách giao dịch trong khoảng thời gian.
 * Dùng cho bộ lọc Ngày / Tuần / Tháng / Năm trên HomeScreen.
 *
 * @param from epoch millis, inclusive
 * @param to   epoch millis, exclusive
 */
class GetTransactionsByPeriodUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(from: Long, to: Long): Flow<List<TransactionModel>> =
        repository.getTransactionsByPeriod(from, to)
}
