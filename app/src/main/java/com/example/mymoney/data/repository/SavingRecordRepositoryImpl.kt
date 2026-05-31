package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.SavingRecordDao
import com.example.mymoney.data.local.entity.SavingRecordEntity
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.domain.model.SavingRecordModel
import com.example.mymoney.domain.repository.SavingRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SavingRecordRepositoryImpl(
    private val dao: SavingRecordDao
) : SavingRecordRepository {

    override fun getRecordsByGoalId(goalId: Long): Flow<List<SavingRecordModel>> =
        dao.getRecordsByGoalId(goalId)
            .map { list -> list.map { it.toModel() } }

    override suspend fun getRecordById(recordId: Long): SavingRecordModel? {
        return dao.getRecordById(recordId)?.toModel()
    }

    override fun getTotalAmountByGoalId(goalId: Long): Flow<Double> {
        return dao.getRecordsByGoalId(goalId)
            .map { list ->
                list.filter { !it.isDeleted }
                    .sumOf { it.amount }
            }
    }
    override suspend fun addRecord(record: SavingRecordModel): Long {
        val now = System.currentTimeMillis()
        return dao.insertRecord(record.toEntityForInsert(now))
    }

    override suspend fun updateRecord(record: SavingRecordModel) {
        val now = System.currentTimeMillis()
        dao.updateRecord(record.toEntityForUpdate(now))
    }

    override suspend fun deleteRecord(id: Long) {
        dao.softDelete(id)
    }

    override suspend fun deleteRecordsByGoalId(goalId: Long) {
        val records = dao.getRecordsByGoalId(goalId).first()
        records.forEach {
            dao.softDelete(it.id)
        }
    }

    private fun SavingRecordEntity.toModel() = SavingRecordModel(
        id = id,
        userId = userId,
        savingGoalId = savingGoalId,
        amount = amount,
        note = note,
        recordDate = recordDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun SavingRecordModel.toEntityForInsert(now: Long) =
        SavingRecordEntity(
            id = id,
            userId = userId,
            savingGoalId = savingGoalId,
            amount = amount,
            note = note,
            recordDate = recordDate,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
            syncStatus = SyncStatus.PENDING_INSERT
        )

    private fun SavingRecordModel.toEntityForUpdate(now: Long) =
        SavingRecordEntity(
            id = id,
            userId = userId,
            savingGoalId = savingGoalId,
            amount = amount,
            note = note,
            recordDate = recordDate,
            createdAt = createdAt,
            updatedAt = now,
            isDeleted = false,
            syncStatus = SyncStatus.PENDING_UPDATE
        )
}