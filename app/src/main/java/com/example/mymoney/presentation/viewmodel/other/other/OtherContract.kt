package com.example.mymoney.presentation.viewmodel.other.other

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho OtherScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trạng thái giao diện của màn hình Khác.
 * Dùng data class bất biến – cập nhật bằng copy().
 */
data class OtherUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ OtherScreen lên ViewModel.
 */
sealed interface OtherEvent
