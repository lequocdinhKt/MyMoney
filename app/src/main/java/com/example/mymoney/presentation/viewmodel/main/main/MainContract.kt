package com.example.mymoney.presentation.viewmodel.main.main

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho MainScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trạng thái giao diện của màn hình chính (shell BottomNav).
 * Dùng data class bất biến – cập nhật bằng copy().
 */
data class MainUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ MainScreen lên ViewModel.
 */
sealed interface MainEvent
