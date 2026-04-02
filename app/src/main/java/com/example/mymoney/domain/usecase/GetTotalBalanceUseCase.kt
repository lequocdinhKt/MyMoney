package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case: Lấy tổng số dư tất cả ví của user.
 */
class GetTotalBalanceUseCase(
    private val repository: WalletRepository
) {
    operator fun invoke(userId: String): Flow<Double> =
        repository.getTotalBalance(userId)
}
