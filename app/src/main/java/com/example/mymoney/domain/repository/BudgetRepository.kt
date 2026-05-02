package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.BudgetModel
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgets(userId: String, month: Int, year: Int): Flow<List<BudgetModel>>
    suspend fun getBudget(userId: String, categoryId: Long, month: Int, year: Int): BudgetModel?
    suspend fun saveBudget(budget: BudgetModel): Long
    suspend fun updateBudget(budget: BudgetModel)
    suspend fun deleteBudget(id: Long)
}

