package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.BudgetDao
import com.example.mymoney.data.local.entity.BudgetEntity
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.domain.model.BudgetModel
import com.example.mymoney.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgets(userId: String, month: Int, year: Int): Flow<List<BudgetModel>> =
        budgetDao.observeBudgets(userId, month, year).map { list -> list.map { it.toModel() } }

    override suspend fun getBudget(userId: String, categoryId: Long, month: Int, year: Int): BudgetModel? =
        budgetDao.getBudget(userId, categoryId, month, year)?.toModel()

    override suspend fun getBudgetById(id: Long): BudgetModel? =
        budgetDao.getBudgetById(id)?.toModel()

    override suspend fun saveBudget(budget: BudgetModel): Long =
        budgetDao.insert(budget.toEntity())

    override suspend fun updateBudget(budget: BudgetModel) =
        budgetDao.update(budget.toEntity().copy(syncStatus = SyncStatus.PENDING_UPDATE))

    override suspend fun deleteBudget(id: Long) =
        budgetDao.softDelete(id)

    // ── Mappers ──

    private fun BudgetEntity.toModel() = BudgetModel(
        id          = id,
        userId      = userId,
        categoryId  = categoryId,
        amountLimit = amountLimit,
        month       = month,
        year        = year,
        createdAt   = createdAt,
        updatedAt   = updatedAt,
        supabaseId  = supabaseId
    )

    private fun BudgetModel.toEntity(): BudgetEntity {
        val now = System.currentTimeMillis()
        return BudgetEntity(
            id          = id,
            supabaseId  = supabaseId,
            userId      = userId,
            categoryId  = categoryId,
            amountLimit = amountLimit,
            month       = month,
            year        = year,
            createdAt   = createdAt,
            updatedAt   = now,
            isDeleted   = false,
            syncStatus  = SyncStatus.PENDING_INSERT
        )
    }
}

