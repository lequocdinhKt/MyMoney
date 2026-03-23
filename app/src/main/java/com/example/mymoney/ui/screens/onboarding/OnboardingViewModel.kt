package com.example.mymoney.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ────────────────────────────────────────────────────────────
// Số trang onboarding (không đổi)
// ────────────────────────────────────────────────────────────
const val ONBOARDING_TOTAL_PAGES = 3

// ────────────────────────────────────────────────────────────
// Event: hành động người dùng gửi tới ViewModel
// ────────────────────────────────────────────────────────────
sealed class OnboardingEvent {
    /** Người dùng nhấn nút "Tiếp theo" */
    data object OnNextClicked : OnboardingEvent()

    /** Người dùng nhấn nút "Quay lại" */
    data object OnPreviousClicked : OnboardingEvent()

    /** UI đã xử lý xong navigation → reset cờ để tránh navigate lặp */
    data object OnNavigationHandled : OnboardingEvent()
}

// ────────────────────────────────────────────────────────────
// State: trạng thái UI quan sát được
// ────────────────────────────────────────────────────────────
/**
 * Trạng thái của màn hình onboarding thống nhất.
 *
 * @param currentPage Trang hiện tại (0-based, tối đa ONBOARDING_TOTAL_PAGES - 1)
 * @param navigateToMain Cờ báo hiệu UI cần chuyển sang MainScreen
 */
data class OnboardingUiState(
    val currentPage: Int = 0,
    val navigateToMain: Boolean = false
)

// ────────────────────────────────────────────────────────────
// ViewModel: xử lý logic chuyển trang và navigation
// ────────────────────────────────────────────────────────────
/**
 * ViewModel cho OnboardingScreen thống nhất.
 *
 * Luồng UDF:
 *   UI gửi OnboardingEvent → ViewModel cập nhật OnboardingUiState
 *   → UI quan sát state → render đúng trang / thực hiện navigate
 */
class OnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    /** State cho UI quan sát (chỉ đọc) */
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /** Nhận sự kiện từ UI và xử lý */
    fun onEvent(event: OnboardingEvent) {
        when (event) {
            // Nhấn "Tiếp theo": nếu là trang cuối → bật cờ navigate, ngược lại tăng trang
            is OnboardingEvent.OnNextClicked -> {
                val current = _uiState.value.currentPage
                if (current >= ONBOARDING_TOTAL_PAGES - 1) {
                    _uiState.update { it.copy(navigateToMain = true) }
                } else {
                    _uiState.update { it.copy(currentPage = current + 1) }
                }
            }
            // Nhấn "Quay lại": giảm trang (không xuống dưới 0)
            is OnboardingEvent.OnPreviousClicked -> {
                val current = _uiState.value.currentPage
                if (current > 0) {
                    _uiState.update { it.copy(currentPage = current - 1) }
                }
            }
            // UI đã navigate xong → tắt cờ để tránh navigate lại khi recompose
            is OnboardingEvent.OnNavigationHandled -> {
                _uiState.update { it.copy(navigateToMain = false) }
            }
        }
    }
}
