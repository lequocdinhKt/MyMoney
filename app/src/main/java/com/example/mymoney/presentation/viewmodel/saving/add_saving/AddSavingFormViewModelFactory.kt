package com.example.mymoney.presentation.viewmodel.saving.add_saving

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.SavingRepositoryImpl
import com.example.mymoney.domain.usecase.AddSavingGoalUserCase

class AddSavingFormViewModelFactory(
    private val context: Context,
    private val userId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddSavingFormViewModel::class.java)) {
            val db = AppDatabase.getInstance(context.applicationContext)
            val repository = SavingRepositoryImpl(savingDao = db.savingDao())
            val addSavingUseCase = AddSavingGoalUserCase(repository = repository)
            val settingPrefs = SettingPreferences(context.applicationContext)
            return AddSavingFormViewModel(
                userId = userId,
                addSavingUseCase = addSavingUseCase,
                settingPrefs
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}