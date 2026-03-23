package com.example.mymoney.ui.screens.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.static.onboardingPages
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ────────────────────────────────────────────────────────────
// ViewModel: chỉ chứa logic xử lý — data/contract đã tách riêng
// ────────────────────────────────────────────────────────────

/**
 * ViewModel cho OnboardingScreen.
 *
 * Nhận [SettingPreferences] qua constructor để lưu trạng thái
 * "đã xem onboarding" vào DataStore mà không phụ thuộc trực tiếp vào Context.
 *
 * Luồng UDF:
 *   UI gửi [OnboardingEvent] → ViewModel cập nhật [OnboardingUiState]
 *   hoặc phát [OnboardingNavEvent] qua SharedFlow → UI collect và navigate
 *
 * @param settingPreferences Truy cập DataStore để đọc/ghi cài đặt ứng dụng
 */
class OnboardingViewModel(
    private val settingPreferences: SettingPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    /** State cho UI quan sát — chỉ đọc */
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // replay = 0: không phát lại event cũ khi subscriber mới collect
    private val _navEvent = MutableSharedFlow<OnboardingNavEvent>()

    /** Navigation side-effect — UI collect 1 lần qua LaunchedEffect(Unit) */
    val navEvent: SharedFlow<OnboardingNavEvent> = _navEvent.asSharedFlow()

    // ── Xử lý event từ UI ──

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.OnNextClicked -> handleNextClicked()
        }
    }

    /**
     * Xử lý nhấn "Tiếp theo":
     * - Chưa phải trang cuối → tăng currentPage
     * - Trang cuối → lưu IS_ONBOARDING_COMPLETED = true → phát NavigateToMain
     */
    private fun handleNextClicked() {
        val current = _uiState.value.currentPage
        if (current < onboardingPages.lastIndex) {
            // Chưa phải trang cuối → sang trang tiếp theo
            _uiState.update { it.copy(currentPage = current + 1) }
        } else {
            // Trang cuối → lưu preference TRƯỚC, sau đó navigate
            viewModelScope.launch {
                settingPreferences.saveOnboardingCompleted()      // ghi DataStore
                _navEvent.emit(OnboardingNavEvent.NavigateToMain) // báo UI navigate
            }
        }
    }

    // ────────────────────────────────────────────────────────────
    // Factory: tạo ViewModel với dependency SettingPreferences
    // Dùng thủ công khi chưa có DI framework (Hilt/Koin)
    // ────────────────────────────────────────────────────────────

    companion object {
        /**
         * Tạo [ViewModelProvider.Factory] nhận [Context].
         *
         * Cách dùng trong Composable:
         * ```kotlin
         * val ctx = LocalContext.current
         * val vm: OnboardingViewModel = viewModel(factory = OnboardingViewModel.factory(ctx))
         * ```
         */
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    OnboardingViewModel(
                        settingPreferences = SettingPreferences(context.applicationContext)
                    ) as T
            }
    }
}


