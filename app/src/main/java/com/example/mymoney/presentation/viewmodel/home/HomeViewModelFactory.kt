package com.example.mymoney.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    private val userId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java))
        return HomeViewModel(
            getTransactionsByPeriod = getTransactionsByPeriod,
            getPeriodSummary        = getPeriodSummary,
            getTotalBalance         = getTotalBalance,
            userId                  = userId
        ) as T
    }
}
