package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.SavingGoalDetailModel
import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.repository.SavingRepository
import kotlinx.coroutines.flow.first

class GetSavingGoalDetailUseCase(
    private val goalRepository: SavingRepository,
    private val recordRepository: SavingRecordRepository
) {
    suspend operator fun invoke(goalId: Long): SavingGoalDetailModel? {
        val goal = goalRepository.getSavingGoalById(goalId) ?: return null
        val records = recordRepository.getRecordsByGoalId(goalId).first()
        val totalSaved = records.sumOf { it.amount }
        val progress = (totalSaved / goal.targetAmount).toFloat().coerceIn(0f, 1f)

        return SavingGoalDetailModel(
            goal = goal,
            records = records,
            progress = progress,
            remainingAmount = goal.targetAmount - totalSaved
        )
    }
}