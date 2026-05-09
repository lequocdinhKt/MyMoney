package com.example.mymoney.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.domain.repository.TransactionRepository
import com.example.mymoney.domain.repository.WalletRepository
import com.example.mymoney.domain.usecase.GetPeriodSummaryUseCase
import com.example.mymoney.domain.usecase.GetTotalBalanceUseCase
import com.example.mymoney.domain.usecase.GetTransactionsByPeriodUseCase

/**
 * Factory inject use-cases vào HomeViewModel thủ công (không dùng Hilt).
 */
class HomeViewModelFactory(
    private val getTransactionsByPeriod: GetTransactionsByPeriodUseCase,
    private val getPeriodSummary: GetPeriodSummaryUseCase,
    private val getTotalBalance: GetTotalBalanceUseCase,
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val userId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java))
        return HomeViewModel(
            getTransactionsByPeriod = getTransactionsByPeriod,
            getPeriodSummary        = getPeriodSummary,
            getTotalBalance         = getTotalBalance,
            walletRepository        = walletRepository,
            transactionRepository   = transactionRepository,
            userId                  = userId
        ) as T
    }
}
