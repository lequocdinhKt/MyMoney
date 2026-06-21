package com.example.mymoney.presentation.viewmodel.saving.add_saving_record

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.SavingRecordRepositoryImpl
import com.example.mymoney.data.repository.SavingRepositoryImpl
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.data.repository.WalletRepositoryImpl
import com.example.mymoney.domain.usecase.AddSavingRecordUseCase

class AddSavingRecordViewModelFactory (
    private val context: Context,
    private val userId: String,
    private val goalId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddSavingRecordViewModel::class.java)) {
            val db = AppDatabase.getInstance(context.applicationContext)
            val walletRepository = WalletRepositoryImpl(walletDao = db.walletDao())
            val savingRecordRepository = SavingRecordRepositoryImpl(dao = db.savingRecordDao())
            val savingRepository = SavingRepositoryImpl(savingDao = db.savingDao())
            val transactionRepository = TransactionRepositoryImpl(transactionDao = db.transactionDao())

            val addSavingRecordUseCase = AddSavingRecordUseCase(
                walletRepository = walletRepository,
                savingRecordRepository = savingRecordRepository,
                savingRepository = savingRepository,
                transactionRepository = transactionRepository
            )
            return AddSavingRecordViewModel(
                walletRepository = walletRepository,
                addSavingRecordUseCase = addSavingRecordUseCase,
                userId = userId,
                goalId = goalId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}