package com.example.mymoney.presentation.viewmodel.saving

import androidx.lifecycle.ViewModel
import com.example.mymoney.presentation.viewmodel.saving.saving.SavingEvent
import com.example.mymoney.presentation.viewmodel.saving.saving.SavingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
