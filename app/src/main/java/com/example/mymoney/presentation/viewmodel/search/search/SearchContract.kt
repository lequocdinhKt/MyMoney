package com.example.mymoney.presentation.viewmodel.search.search

import com.example.mymoney.domain.model.TransactionModel

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho SearchScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trạng thái giao diện của màn hình Tìm kiếm.
 * Dùng data class bất biến – cập nhật bằng copy().
 */
data class SearchUiState(
    val query: String = "", // Từ khóa tìm kiếm khi user nhập vào - Mặc định là rỗng

    // Loại bộ lọc được chọn - Mặc định là All (không lọc, lấy tất cả)
    val selectedFilter: FilterType = FilterType.All,

     //Danh sách các giao dịch - Mặc định là danh sách rỗng
    val transactions: List<TransactionModel> = emptyList()
)

enum class FilterType {
    All, NAME, CATEGORY, DAY, MONTH, YEAR
}

/**
 * Sự kiện người dùng gửi từ SearchScreen lên ViewModel.
 */
sealed interface SearchEvent {
    data class onQueryChange(val query: String): SearchEvent
    data class onFilterChange(val filter: FilterType): SearchEvent

}
