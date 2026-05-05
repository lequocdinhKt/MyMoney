package com.example.mymoney.presentation.viewmodel.budget_details.budget_detail

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho BudgetManualScreen
// ─────────────────────────────────────────────────────────────────────────────

data class BudgetManualContract(
    val isLoading: Boolean = true,
)

sealed interface BudgetManuelEvent {

}