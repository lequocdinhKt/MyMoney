package com.example.mymoney.presentation.viewmodel.budget.budget

import com.example.mymoney.domain.model.BudgetModel

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho BudgetScreen
// ─────────────────────────────────────────────────────────────────────────────

data class BudgetUiState(
    val isLoading: Boolean = true,
    // Lưu trạng thái có mở "Thêm ngân sách" không - Mặc định là false
    val showAddBudget: Boolean = false,
    val budgets: List<BudgetModel> = emptyList()
)

sealed interface BudgetEvent {
    data class SaveBudget(val budget: BudgetModel) : BudgetEvent
    data class DeleteBudget(val id: Long) : BudgetEvent
    data class LoadMonth(val month: Int, val year: Int) : BudgetEvent


    data object AddBudgetClicked : BudgetEvent
    data object AddBudgetDismissed : BudgetEvent
}
