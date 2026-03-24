package com.example.mymoney.presentation.viewmodel.home.home

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho HomeScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trạng thái giao diện của màn hình Trang chủ.
 * Dùng data class bất biến – cập nhật bằng copy().
 */
data class HomeUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ HomeScreen lên ViewModel.
 * Hiện tại chưa có event – mở rộng khi cần.
 */
sealed interface HomeEvent
