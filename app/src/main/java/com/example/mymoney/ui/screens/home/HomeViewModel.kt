package com.example.mymoney.ui.screens.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trạng thái giao diện của màn hình Trang chủ.
 * Dùng data class bất biến — cập nhật bằng copy().
 */
data class HomeUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ HomeScreen lên ViewModel.
 * Hiện tại chưa có event — mở rộng khi cần.
 */
sealed interface HomeEvent

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Trang chủ.
 * UI chỉ observe [uiState] — không chứa logic nghiệp vụ.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    /** State duy nhất mà UI observe */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Xử lý sự kiện từ UI */
    fun onEvent(event: HomeEvent) {
        // TODO: Xử lý event khi có thêm chức năng
    }
}
