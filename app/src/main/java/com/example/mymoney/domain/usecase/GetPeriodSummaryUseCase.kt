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
    fun getIncome(userId: String, from: Long, to: Long): Flow<Double> =
        repository.getTotalIncome(userId, from, to)

    fun getExpense(userId: String, from: Long, to: Long): Flow<Double> =
        repository.getTotalExpense(userId, from, to)

    fun getIncomeByWallet(userId: String, walletId: Long, from: Long, to: Long): Flow<Double> =
        repository.getTotalIncomeByWallet(userId, walletId, from, to)

    fun getExpenseByWallet(userId: String, walletId: Long, from: Long, to: Long): Flow<Double> =
        repository.getTotalExpenseByWallet(userId, walletId, from, to)
}
