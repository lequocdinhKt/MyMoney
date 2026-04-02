package com.example.mymoney.presentation.viewmodel.home.home

import androidx.compose.runtime.Immutable

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho HomeScreen
// ─────────────────────────────────────────────────────────────────────────────

/** Bộ lọc khoảng thời gian hiển thị giao dịch */
enum class TimePeriod(val label: String) {
    DAY("Ngày"),
    WEEK("Tuần"),
    MONTH("Tháng"),
    YEAR("Năm"),
    CUSTOM("...")
}

/**
 * Model hiển thị một dòng giao dịch.
 * @Immutable giúp Compose bỏ qua recompose khi object không thay đổi.
 */
@Immutable
data class TransactionItem(
    val id: String,
    val categoryIconRes: Int?,      // resource id icon, null → hiện placeholder
    val title: String,
    val dateTime: String,           // đã format sẵn, ví dụ "7:00, 11/03/2026"
    val amount: Long,               // âm = chi tiêu, dương = thu nhập (đơn vị: VND)
    val formattedAmount: String     // đã format sẵn, ví dụ "-1.000.000 vnđ"
)

/**
 * Trạng thái giao diện của màn hình Trang chủ.
 * Dùng data class bất biến – cập nhật bằng copy().
 */
@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,          // true = đang load, tránh flash dữ liệu cũ
    val balance: Long = 0L,
    val formattedBalance: String = "0 vnđ",
    val selectedPeriod: TimePeriod = TimePeriod.DAY,
    val groupLabel: String = "",
    val totalIncome: String = "0",
    val totalExpense: String = "0",
    val totalBalance: String = "0",
    val transactions: List<TransactionItem> = emptyList()
)

/**
 * Sự kiện người dùng gửi từ HomeScreen lên ViewModel.
 */
sealed interface HomeEvent {
    data class SelectPeriod(val period: TimePeriod) : HomeEvent
    data object AddTransactionClick : HomeEvent
}
