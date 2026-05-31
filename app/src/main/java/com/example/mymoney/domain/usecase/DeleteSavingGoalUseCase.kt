package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.repository.SavingRepository
import kotlinx.coroutines.flow.first

class DeleteSavingGoalUseCase (
    private val goalRepository: SavingRepository,
    private val recordRepository: SavingRecordRepository
) {
    suspend operator fun invoke(goalId: Long) {
        recordRepository.deleteRecordsByGoalId(goalId)
        goalRepository.deleteSavingGoal(goalId)
    }
}