package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.SavingRecordModel
import kotlinx.coroutines.flow.Flow

interface SavingRecordRepository {
    fun getRecordsByGoalId(goalId: Long): Flow<List<SavingRecordModel>>
    fun getTotalAmountByGoalId(goalId: Long): Flow<Double>
    suspend fun getRecordById(recordId: Long): SavingRecordModel?
    suspend fun addRecord(savingRecord: SavingRecordModel): Long
    suspend fun updateRecord(savingRecord: SavingRecordModel)
    suspend fun deleteRecord(id: Long)
    suspend fun deleteRecordsByGoalId(goalId: Long)
}