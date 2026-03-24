package com.example.mymoney.presentation.viewmodel.onboarding.onboarding

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event, NavEvent cho OnboardingScreen
// Tách riêng để ViewModel và Screen không import lẫn nhau
// ─────────────────────────────────────────────────────────────────────────────

// ── UI State ──

/**
 * Trạng thái bất biến của màn hình onboarding.
 * ViewModel cập nhật bằng copy(), UI chỉ đọc.
 *
 * @param currentPage Trang đang hiển thị (0-based)
 */
data class OnboardingUiState(
    val currentPage: Int = 0,
)

// ── UI Event ──

/**
 * Hành động người dùng gửi từ UI lên ViewModel.
 * Chỉ có OnNextClicked – không có Previous vì UI không dùng.
 */
sealed class OnboardingEvent {
    /** Người dùng nhấn nút "Tiếp theo" */
    data object OnNextClicked : OnboardingEvent()
}

// ── Navigation Side-Effect ──

/**
 * Side-effect điều hướng phát qua SharedFlow.
 * Không lưu trong UiState – tránh xử lý lại khi recompose.
 */
sealed class OnboardingNavEvent {
    /** Hoàn thành onboarding để điều hướng sang MainScreen */
    data object NavigateToMain : OnboardingNavEvent()
}
