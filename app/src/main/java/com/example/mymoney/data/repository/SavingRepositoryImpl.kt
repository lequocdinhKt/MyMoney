package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.SavingDao
import com.example.mymoney.data.local.entity.SavingGoalEntity
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.domain.model.SavingGoalModel
import com.example.mymoney.domain.model.SavingType
import com.example.mymoney.domain.repository.SavingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingRepositoryImpl(
    private val savingDao: SavingDao
) : SavingRepository {

    override fun getSavingGoals(userId: String): Flow<List<SavingGoalModel>> =
        savingDao.observeSavingGoals(userId)
            .map { list -> list.map { it.toModel() } }

    override suspend fun getSavingGoalById(savingGoalId: Long): SavingGoalModel? =
        savingDao.getSavingGoalById(savingGoalId)?.toModel()

    override suspend fun addSavingGoal(savingGoal: SavingGoalModel): Long {
        val now = System.currentTimeMillis()
        return savingDao.insert(savingGoal.toEntityForInsert(now))
    }

    override suspend fun updateSavingGoal(savingGoal: SavingGoalModel) {
        val now = System.currentTimeMillis()
        savingDao.update(savingGoal.toEntityForUpdate(now))
    }

    override suspend fun deleteSavingGoal(id: Long) {
        savingDao.softDelete(id)
    }

    private fun SavingGoalEntity.toModel() = SavingGoalModel(
        id = id,
        userId = userId,
        title = title,
        currency = currency,
        targetAmount = targetAmount,
        savingType = runCatching { SavingType.valueOf(savingType) }.getOrDefault(SavingType.ONE_TIME),
        targetDate = targetDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        supabaseId = supabaseId
    )

    private fun SavingGoalModel.toEntityForInsert(now: Long) =
        SavingGoalEntity(
            id = id,
            userId = userId,
            title = title,
            currency = currency,
            savingType = savingType.name,
            targetAmount = targetAmount,
            targetDate = targetDate,
            createdAt = now,
            updatedAt = now,
            supabaseId = supabaseId,
            syncStatus = SyncStatus.PENDING_INSERT
        )

    private fun SavingGoalModel.toEntityForUpdate(now: Long) =
        SavingGoalEntity(
            id = id,
            userId = userId,
            title = title,
            currency = currency,
            savingType = savingType.name,
            targetAmount = targetAmount,
            targetDate = targetDate,
            createdAt = createdAt,
            updatedAt = now,
            supabaseId = supabaseId,
            syncStatus = SyncStatus.PENDING_UPDATE
        )
}