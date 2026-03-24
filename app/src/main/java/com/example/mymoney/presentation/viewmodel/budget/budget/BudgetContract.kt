package com.example.mymoney.presentation.viewmodel.budget.budget

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho BudgetScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trạng thái giao diện của màn hình Ngân sách.
 * Dùng data class bất biến – cập nhật bằng copy().
 */
data class BudgetUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ BudgetScreen lên ViewModel.
 */
sealed interface BudgetEvent
