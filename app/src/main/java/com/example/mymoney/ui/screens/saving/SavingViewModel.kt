package com.example.mymoney.ui.screens.saving

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trạng thái giao diện của màn hình Tiết kiệm.
 * Dùng data class bất biến — cập nhật bằng copy().
 */
data class SavingUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ SavingScreen lên ViewModel.
 */
sealed interface SavingEvent

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Tiết kiệm.
 */
class SavingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SavingUiState())

    /** State duy nhất mà UI observe */
    val uiState: StateFlow<SavingUiState> = _uiState.asStateFlow()

    /** Xử lý sự kiện từ UI */
    fun onEvent(event: SavingEvent) {
        // TODO: Xử lý event khi có thêm chức năng
    }
}
