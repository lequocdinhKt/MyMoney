package com.example.mymoney.presentation.viewmodel.budget.budget

import com.example.mymoney.domain.model.CategoryModel

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho BudgetManualScreen
// ─────────────────────────────────────────────────────────────────────────────

data class BudgetManualUiState(
    val id: Long = 0L,

    val selectedCategory: CategoryModel? = null,
    val categories: List<CategoryModel> = emptyList(),

    val originalAmountLimit: String = "", // giới hạn chi tiêu ban đầu khi mới tạo và sẽ được dùng để so sánh sau này
    val currentAmountLimit: String = "", // giới hạn chi tiêu sau khi chỉnh sửa
    val month: Int = 0,
    val year: Int = 0,
    val error: String? = null,
    val createdAt: Long = 0L,

    val showDeleteDialog: Boolean = false, // Hiện dialog xóa ngân sách
    val showCategorySheet: Boolean = false, // Hiện ô lựa chọn danh mục

    val isEditMode: Boolean = false,
    val isDeleting: Boolean = false, // Đang xóa
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

sealed interface BudgetManualEvent {
    data class OnAmountChange(val value: String) : BudgetManualEvent
    data class OnCategorySelected(val category: CategoryModel) : BudgetManualEvent
    data class OnMonthSelected(val month: Int) : BudgetManualEvent
    data class OnYearSelected(val year: Int) : BudgetManualEvent
    data object Save : BudgetManualEvent
    data object DeleteClicked : BudgetManualEvent // Khi nhấn nút xóa thì hiện dialog
    data object DeleteConfirm : BudgetManualEvent // Xác nhận bằng cách nhấn nút xóa trên dialog
    data object DeleteDismissed : BudgetManualEvent // Nhấn nút hủy hoặc click bên ngoài dialog
    data object CategoryClicked : BudgetManualEvent
    data object DismissCategorySheet : BudgetManualEvent
    data object DismissError : BudgetManualEvent
    data object ClearCategory : BudgetManualEvent
}