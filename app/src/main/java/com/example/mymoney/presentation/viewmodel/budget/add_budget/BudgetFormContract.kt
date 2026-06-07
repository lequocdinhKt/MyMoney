package com.example.mymoney.presentation.viewmodel.budget.add_budget

import com.example.mymoney.domain.model.CategoryModel

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho BudgetFormScreen
// ─────────────────────────────────────────────────────────────────────────────

data class BudgetFormUiState(
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

sealed interface BudgetFormEvent {
    data class OnAmountChange(val value: String) : BudgetFormEvent
    data class OnCategorySelected(val category: CategoryModel) : BudgetFormEvent
    data class OnMonthSelected(val month: Int) : BudgetFormEvent
    data class OnYearSelected(val year: Int) : BudgetFormEvent
    data object Save : BudgetFormEvent
    data object DeleteClicked : BudgetFormEvent // Khi nhấn nút xóa thì hiện dialog
    data object DeleteConfirm : BudgetFormEvent  // Xác nhận bằng cách nhấn nút xóa trên dialog
    data object DeleteDismissed : BudgetFormEvent // Nhấn nút hủy hoặc click bên ngoài dialog
    data object CategoryClicked : BudgetFormEvent
    data object DismissCategorySheet : BudgetFormEvent
    data object DismissError : BudgetFormEvent
    data object ClearCategory : BudgetFormEvent
}