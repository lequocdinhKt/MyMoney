package com.example.mymoney.presentation.viewmodel.recurring

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.WalletRepositoryImpl
import com.example.mymoney.domain.usecase.EnsureDefaultWalletUseCase

class RecurringViewModelFactory(
    private val context: Context,
    private val walletId: Long = 0L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RecurringViewModel::class.java)) {
            "Unknown ViewModel: ${modelClass.name}"
        }
        val appCtx = context.applicationContext
        val db = AppDatabase.getInstance(appCtx)
        val walletRepo = WalletRepositoryImpl(db.walletDao())
        return RecurringViewModel(
            recurringDao       = db.recurringTransactionDao(),
            walletRepository   = walletRepo,
            ensureDefaultWallet = EnsureDefaultWalletUseCase(walletRepo),
            settingPreferences = SettingPreferences(appCtx),
            selectedWalletId   = walletId
        ) as T
    }
}

