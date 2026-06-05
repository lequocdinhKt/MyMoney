package com.example.mymoney.presentation.viewmodel.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.domain.repository.TransactionRepository

class StreakViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(StreakViewModel::class.java))
        return StreakViewModel(transactionRepository, userId) as T
    }
}

