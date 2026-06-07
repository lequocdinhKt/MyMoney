package com.example.mymoney.presentation.viewmodel.budget.add_budget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.BudgetRepositoryImpl
import com.example.mymoney.data.repository.CategoryRepositoryImpl

/**
 * Factory tạo [BudgetManualViewModel] với dependencies thủ công (không Hilt).
 *
 * @param context   ApplicationContext
 * @param userId    ID người dùng hiện tại
 * @param budgetId  null = thêm mới, khác null = chỉnh sửa ngân sách có id này
 */

class BudgetFormViewModelFactory(
    private val context: Context,
    private val userId: String,
    private val budgetId: Long? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BudgetFormViewModel::class.java))
        val db   = AppDatabase.Companion.getInstance(context.applicationContext)
        val budgetRepo = BudgetRepositoryImpl(db.budgetDao())
        val categoryRepo = CategoryRepositoryImpl(db.categoryDao())
        val settingPrefs = SettingPreferences(context.applicationContext)
        return BudgetFormViewModel(
            budgetRepository = budgetRepo,
            categoryRepository = categoryRepo,
            settingPreferences = settingPrefs,
            userId = userId,
            budgetId = budgetId
        ) as T
    }
}