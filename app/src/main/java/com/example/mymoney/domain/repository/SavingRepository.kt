package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.SavingGoalModel
import kotlinx.coroutines.flow.Flow

interface SavingRepository {
    fun getSavingGoals(userId: String): Flow<List<SavingGoalModel>>
    suspend fun getSavingGoalById(savingGoalId: Long): SavingGoalModel?
    suspend fun addSavingGoal(savingGoal: SavingGoalModel): Long
    suspend fun updateSavingGoal(savingGoal: SavingGoalModel)
    suspend fun deleteSavingGoal(id: Long)
}