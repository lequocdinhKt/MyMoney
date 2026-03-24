package com.example.mymoney.ui.screens.budget

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trạng thái giao diện của màn hình Ngân sách.
 * Dùng data class bất biến — cập nhật bằng copy().
 */
data class BudgetUiState(
    val isLoading: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ BudgetScreen lên ViewModel.
 */
sealed interface BudgetEvent

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Ngân sách.
 */
class BudgetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())

    /** State duy nhất mà UI observe */
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    /** Xử lý sự kiện từ UI */
    fun onEvent(event: BudgetEvent) {
        // TODO: Xử lý event khi có thêm chức năng
    }
}
