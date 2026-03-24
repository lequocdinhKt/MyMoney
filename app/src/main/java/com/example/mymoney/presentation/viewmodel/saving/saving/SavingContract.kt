package com.example.mymoney.presentation.viewmodel.saving.saving

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho SavingScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trạng thái giao diện của màn hình Tiết kiệm.
 * Dùng data class bất biến – cập nhật bằng copy().
 */
data class SavingUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ SavingScreen lên ViewModel.
 */
sealed interface SavingEvent
