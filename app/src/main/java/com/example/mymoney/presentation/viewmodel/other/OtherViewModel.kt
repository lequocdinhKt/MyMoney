package com.example.mymoney.presentation.viewmodel.other

import androidx.lifecycle.ViewModel
import com.example.mymoney.presentation.viewmodel.other.other.OtherEvent
import com.example.mymoney.presentation.viewmodel.other.other.OtherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Khác.
 */
class OtherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OtherUiState())

    /** State duy nhất mà UI observe */
    val uiState: StateFlow<OtherUiState> = _uiState.asStateFlow()

    /** Xử lý sự kiện từ UI */
    fun onEvent(event: OtherEvent) {
        when (event) {
            is OtherEvent.AboutUsClicked -> {
                // TODO: Điều hướng sang màn hình About
            }
            is OtherEvent.SupportUsClicked -> {
                // TODO: Hiển thị thông tin ủng hộ
            }
            is OtherEvent.ReportBugClicked -> {
                // TODO: Gửi log hoặc mở form báo lỗi
            }
            is OtherEvent.NavigateToAboutUs,
            is OtherEvent.NavigateToReportBug -> {
                // Side-effects handled by UI
            }
        }
    }
}
