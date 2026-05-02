package com.example.mymoney.presentation.viewmodel.budget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.BudgetRepositoryImpl

/**
 * Factory inject dependency chain:
 *   AppDatabase → BudgetDao → BudgetRepositoryImpl → BudgetViewModel
 *
 * @param context  ApplicationContext hoặc Activity context
 * @param userId   ID người dùng hiện tại
 */
class BudgetViewModelFactory(
    private val context: Context,
    private val userId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            "Unknown ViewModel: ${modelClass.name}"
        }
        val db   = AppDatabase.getInstance(context.applicationContext)
        val repo = BudgetRepositoryImpl(db.budgetDao())
        return BudgetViewModel(budgetRepository = repo, userId = userId) as T
    }
}

