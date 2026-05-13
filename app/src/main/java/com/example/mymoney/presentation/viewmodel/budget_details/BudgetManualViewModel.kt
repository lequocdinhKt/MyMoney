package com.example.mymoney.presentation.viewmodel.budget_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.model.BudgetModel
import com.example.mymoney.domain.repository.BudgetRepository
import com.example.mymoney.presentation.viewmodel.budget_details.budget_detail.BudgetManualEvent
import com.example.mymoney.presentation.viewmodel.budget_details.budget_detail.BudgetManualUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Ngân sách khi đuợc thêm thủ công.
 *
 *  * Chế độ:
 *  *  - Tạo mới : budgetId == null  → name rỗng, amountLimit = 0, màu mặc định
 *  *  - Chỉnh sửa: budgetId != null → load dữ liệu từ DB rồi điền vào form
 */

class BudgetManualViewModel(
    private val budgetRepository: BudgetRepository,
    private val userId: String,
    private val budgetId: Long? // null tạo mới
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetManualUiState())
    val uiState: StateFlow<BudgetManualUiState> = _uiState.asStateFlow()

    init {
        if (budgetId != null) {
            loadBudget(budgetId)
        } else {
            val now = LocalDate.now()
            _uiState.update { it.copy(month = now.monthValue, year = now.year) }
        }
    }

    private fun loadBudget(id: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val budget = budgetRepository.getBudgetById(id)
                if (budget != null) {
                    _uiState.update {
                        it.copy(
                            id                   = budget.id,
                            categoryId           = budget.categoryId,
                            currentAmountLimit   = budget.amountLimit.toString(),
                            originalAmountLimit  = budget.amountLimit.toString(),
                            month                = budget.month,
                            year                 = budget.year,
                            createdAt            = budget.createdAt,
                            isEditMode           = true,
                            isLoading            = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isEditMode = true, id = id) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Tải thất bại: ${e.message}") }
            }
        }
    }

    fun onEvent(event: BudgetManualEvent) {
        when (event) {
            is BudgetManualEvent.OnAmountChange        -> _uiState.update { it.copy(currentAmountLimit = event.value, error = null) }
            is BudgetManualEvent.OnMonthSelected       -> _uiState.update { it.copy(month = event.month) }
            is BudgetManualEvent.OnYearSelected        -> _uiState.update { it.copy(year = event.year) }
            is BudgetManualEvent.Save                  -> saveBudget()
            is BudgetManualEvent.DeleteConfirm         -> deleteBudget()
            is BudgetManualEvent.DeleteClicked         -> _uiState.update { it.copy(showDeleteDialog = true) }
            is BudgetManualEvent.DeleteDismissed       -> _uiState.update { it.copy(showDeleteDialog = false) }
            is BudgetManualEvent.OnCategorySelected    -> _uiState.update { it.copy(categoryId = event.categoryId, showCategorySheet = false) }
            is BudgetManualEvent.CategoryClicked       -> _uiState.update { it.copy(showCategorySheet = true) }
            is BudgetManualEvent.DismissCategorySheet  -> _uiState.update { it.copy(showCategorySheet = false) }
            is BudgetManualEvent.DismissError          -> _uiState.update { it.copy(error = null) }
            is BudgetManualEvent.ClearCategory         -> _uiState.update { it.copy(categoryId = 0L) }
        }
    }

    private fun saveBudget() {
        val state = _uiState.value
        if (state.categoryId <= 0L) {
            _uiState.update { it.copy(error = "Vui lòng chọn danh mục") }
            return
        }

        val amountLimit = state.currentAmountLimit.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0

        if (amountLimit <= 0.0) {
            _uiState.update {
                it.copy(error = "Số tiền không hợp lệ")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()

                val budget = BudgetModel(
                    id          = state.id,
                    userId      = userId,
                    categoryId  = state.categoryId,
                    amountLimit = amountLimit,
                    month       = state.month,
                    year        = state.year,
                    createdAt   = if (state.isEditMode) state.createdAt else now,
                    updatedAt   = now,
                )

                if(state.isEditMode) {
                    budgetRepository.updateBudget(budget)
                } else {
                    budgetRepository.saveBudget(budget)
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Lưu thất bại: ${e.message}") }
            }
        }
    }

    private fun deleteBudget() {
        val state = _uiState.value
        if (!state.isEditMode || state.isDeleting) return

        // Bắt đầu xóa
        _uiState.update { it.copy(
            isDeleting = true,
            showDeleteDialog = false,
            error = null
        ) }

        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(state.id)

                _uiState.update { it.copy(
                    isDeleting = false,
                    isDeleted = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isDeleting = false,
                    error = "Xóa thất bại: ${e.message}"
                ) }
            }
        }
    }
}