package com.example.mymoney.presentation.viewmodel.budget

import androidx.lifecycle.ViewModel
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetEvent
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
