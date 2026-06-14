package com.example.mymoney.presentation.viewmodel.saving.saving_detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.SavingRecordRepositoryImpl
import com.example.mymoney.data.repository.SavingRepositoryImpl
import com.example.mymoney.domain.usecase.GetSavingGoalDetailUseCase

class SavingDetailViewModelFactory(
    private val context: Context,
    private val goalId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavingDetailViewModel::class.java)) {
            val db = AppDatabase.getInstance(context.applicationContext)
            val savingRepository = SavingRepositoryImpl(savingDao = db.savingDao())
            val savingRecordRepository = SavingRecordRepositoryImpl(dao = db.savingRecordDao())
            val getSavingGoalDetailUseCase = GetSavingGoalDetailUseCase(
                    goalRepository = savingRepository,
                    recordRepository = savingRecordRepository
                )
            return SavingDetailViewModel(
                savingRecordRepository = savingRecordRepository,
                getSavingGoalDetailUseCase = getSavingGoalDetailUseCase,
                goalId = goalId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}