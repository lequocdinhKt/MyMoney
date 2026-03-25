package com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction

import com.example.mymoney.domain.model.TransactionModel

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event, NavEvent cho AddTransactionScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trạng thái giao diện màn hình Thêm Giao Dịch.
 * Immutable data class — cập nhật bằng copy().
 *
 * @param transactions    Danh sách giao dịch hiện có (observe từ Room)
 * @param noteInput       Nội dung người dùng đang nhập trong ô ghi chú
 * @param isLoading       Đang tải dữ liệu ban đầu hay không
 * @param isEmpty         true khi không có giao dịch nào
 * @param walletName      Tên ví hiện tại (hiển thị trên top bar)
 * @param errorMessage    Thông báo lỗi (null = không lỗi)
 */
data class AddTransactionUiState(
    val transactions: List<TransactionModel> = emptyList(),
    val noteInput: String = "",
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
    val walletName: String = "Ví chính",
    val errorMessage: String? = null
)

/**
 * Sự kiện người dùng gửi từ UI lên ViewModel.
 * Sealed class — mỗi hành động là 1 subclass.
 */
sealed class AddTransactionEvent {
    /** Người dùng thay đổi nội dung ô nhập ghi chú */
    data class OnNoteChanged(val note: String) : AddTransactionEvent()

    /** Người dùng nhấn nút gửi (nút mũi tên xanh) */
    data object OnSubmitClicked : AddTransactionEvent()

    /** Người dùng nhấn nút camera */
    data object OnCameraClicked : AddTransactionEvent()

    /** Người dùng nhấn nút microphone */
    data object OnMicClicked : AddTransactionEvent()

    /** Người dùng nhấn nút settings (cài đặt parsing) */
    data object OnParseSettingsClicked : AddTransactionEvent()

    /** Người dùng nhấn "Di chuyển quỹ" */
    data object OnTransferFundClicked : AddTransactionEvent()

    /** Người dùng nhấn "Giao dịch định kỳ" */
    data object OnRecurringClicked : AddTransactionEvent()
}

/**
 * Side-effect điều hướng — ViewModel phát qua SharedFlow.
 * UI collect 1 lần trong LaunchedEffect(Unit).
 */
sealed class AddTransactionNavEvent {
    /** Quay lại màn hình trước */
    data object NavigateBack : AddTransactionNavEvent()

    /** Mở màn hình cài đặt parsing giao dịch */
    data object NavigateToParseSettings : AddTransactionNavEvent()
}
