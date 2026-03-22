package com.example.mymoney.ui.screens.startScreens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ────────────────────────────────────────────────────────────
// Event: hành động người dùng gửi tới ViewModel
// ────────────────────────────────────────────────────────────

/** Sự kiện từ các màn hình onboarding */
sealed class OnboardingEvent {
    /** Người dùng nhấn nút "Tiếp theo" */
    data object OnNextClicked : OnboardingEvent()

    /** UI đã xử lý xong navigation → reset cờ để tránh navigate lặp */
    data object OnNavigationHandled : OnboardingEvent()
}

// ────────────────────────────────────────────────────────────
// State: trạng thái UI quan sát được
// ────────────────────────────────────────────────────────────

/**
 * Trạng thái của một màn hình onboarding.
 * @param shouldNavigateNext Cờ báo hiệu UI cần thực hiện navigation
 */
data class OnboardingUiState(
    val shouldNavigateNext: Boolean = false
)

// ────────────────────────────────────────────────────────────
// ViewModel: xử lý logic, phát ra state
// ────────────────────────────────────────────────────────────

/**
 * ViewModel dùng chung cho mỗi màn hình onboarding (Start1, Start2, Start3).
 *
 * Mỗi composable destination trong NavHost sẽ tạo instance riêng
 * (scoped theo navigation entry) → đảm bảo mỗi trang có state độc lập.
 *
 * Luồng UDF:
 *   UI gửi OnboardingEvent → ViewModel cập nhật OnboardingUiState
 *   → UI quan sát state → thực hiện navigate → gửi OnNavigationHandled
 */
class OnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    /** State cho UI quan sát (chỉ đọc) */
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /** Nhận sự kiện từ UI và xử lý */
    fun onEvent(event: OnboardingEvent) {
        when (event) {
            // Người dùng nhấn "Tiếp theo" → bật cờ navigation
            is OnboardingEvent.OnNextClicked -> {
                _uiState.update { it.copy(shouldNavigateNext = true) }
            }
            // UI đã navigate xong → tắt cờ để tránh navigate lại khi recompose
            is OnboardingEvent.OnNavigationHandled -> {
                _uiState.update { it.copy(shouldNavigateNext = false) }
            }
        }
    }
}
