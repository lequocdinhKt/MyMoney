package com.example.mymoney.ui.screens.other

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trạng thái giao diện của màn hình Khác.
 * Dùng data class bất biến — cập nhật bằng copy().
 */
data class OtherUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ OtherScreen lên ViewModel.
 */
sealed interface OtherEvent

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Khác.
 */
class OtherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OtherUiState())

    /** State duy nhất mà UI observe */
    val uiState: StateFlow<OtherUiState> = _uiState.asStateFlow()

    /** Xử lý sự kiện từ UI */
    fun onEvent(event: OtherEvent) {
        // TODO: Xử lý event khi có thêm chức năng
    }
}
