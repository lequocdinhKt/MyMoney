package com.example.mymoney.presentation.viewmodel.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.domain.usecase.GetTransactionsUseCase

/**
 * Factory inject use-cases vào SearchViewModel thủ công (không dùng Hilt).
 */
class SearchViewModelFactory(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(getTransactionsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}