package com.example.mymoney.presentation.viewmodel.saving.saving

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.SavingRecordRepositoryImpl
import com.example.mymoney.data.repository.SavingRepositoryImpl
import com.example.mymoney.data.repository.WalletRepositoryImpl
import com.example.mymoney.domain.usecase.DeleteSavingGoalUseCase

class SavingViewModelFactory(
    private val context: Context,
    private val userId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SavingViewModel::class.java)) {
            "Unknown ViewModel: ${modelClass.name}"
        }
        val db   = AppDatabase.getInstance(context.applicationContext)
        val repo = SavingRepositoryImpl(db.savingDao())
        val recordRepo = SavingRecordRepositoryImpl(db.savingRecordDao())
        val deleteSavingGoalUseCase = DeleteSavingGoalUseCase(
            goalRepository = repo,
            recordRepository = recordRepo,
            walletRepository = WalletRepositoryImpl(walletDao = db.walletDao())
        )
        return SavingViewModel(
            settingPreferences     = SettingPreferences(context.applicationContext),
            savingRepository       = repo,
            recordRepository       = recordRepo,
            userId                 = userId,
            deleteSavingGoalUseCase = deleteSavingGoalUseCase
        )
        as T
    }
}
