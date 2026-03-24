package com.example.mymoney.presentation.viewmodel.main

import androidx.lifecycle.ViewModel
import com.example.mymoney.presentation.viewmodel.main.main.MainEvent
import com.example.mymoney.presentation.viewmodel.main.main.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel quản lý logic và trạng thái cho MainScreen (shell BottomNav).
 */
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())

    /** State duy nhất mà UI observe */
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /** Xử lý sự kiện từ UI */
    fun onEvent(event: MainEvent) {
        // TODO: Xử lý event khi có thêm chức năng
    }
}
