package com.example.mymoney.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import com.example.mymoney.presentation.viewmodel.home.home.HomeEvent
import com.example.mymoney.presentation.viewmodel.home.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Trang chủ.
 * UI chỉ observe [uiState] – không chứa logic nghiệp vụ.
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
