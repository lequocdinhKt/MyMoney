package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case: Lấy tổng thu nhập và chi tiêu trong kỳ.
 * Trả về Pair(income, expense) để tiện combine trong ViewModel.
 */
class GetPeriodSummaryUseCase(
    private val repository: TransactionRepository
) {
    fun getIncome(from: Long, to: Long): Flow<Double> =
        repository.getTotalIncome(from, to)

    fun getExpense(from: Long, to: Long): Flow<Double> =
        repository.getTotalExpense(from, to)
}
