package com.example.mymoney.presentation.viewmodel.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.domain.model.BudgetModel
import com.example.mymoney.domain.repository.BudgetRepository
import com.example.mymoney.domain.repository.CategoryRepository
import com.example.mymoney.domain.usecase.MoneyFormatter
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetManualEvent
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetManualUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val categoryRepository: CategoryRepository,
    private val settingPreferences: SettingPreferences,
    private val userId: String,
    private val budgetId: Long? // null tạo mới
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetManualUiState())
    val uiState: StateFlow<BudgetManualUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {

            categoryRepository.seedDefaultCategories(userId)

            observeCategories()

            if (budgetId != null) {
                loadBudget(budgetId)
            } else {
                val now = LocalDate.now()

                _uiState.update {
                    it.copy(
                        month = now.monthValue,
                        year = now.year
                    )
                }
            }
        }
    }

    private fun loadBudget(id: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val budget = budgetRepository.getBudgetById(id)
                val useGrouping = settingPreferences.isThousandSeparatorEnabled.first()
                if (budget != null) {
                    _uiState.update {
                        it.copy(
                            id                   = budget.id,
                            selectedCategory     = _uiState.value.categories.find { it.id == budget.categoryId },
                            currentAmountLimit   = MoneyFormatter.formatInput(budget.amountLimit.toString(), useGrouping),
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

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository
                .getCategoriesByType(userId, "expense")
                .collect { categories ->

                    _uiState.update {
                        it.copy(categories = categories)
                    }
                }
        }
    }

    fun onEvent(event: BudgetManualEvent) {
        when (event) {
            is BudgetManualEvent.OnAmountChange        -> {
                viewModelScope.launch {
                    val useGrouping = settingPreferences.isThousandSeparatorEnabled.first()
                    val formatted = MoneyFormatter.formatInput(event.value, useGrouping)
                    _uiState.update { it.copy(currentAmountLimit = formatted, error = null) }
                }
            }
            is BudgetManualEvent.OnMonthSelected       -> _uiState.update { it.copy(month = event.month) }
            is BudgetManualEvent.OnYearSelected        -> _uiState.update { it.copy(year = event.year) }
            is BudgetManualEvent.Save                  -> saveBudget()
            is BudgetManualEvent.DeleteConfirm         -> deleteBudget()
            is BudgetManualEvent.DeleteClicked         -> _uiState.update { it.copy(showDeleteDialog = true) }
            is BudgetManualEvent.DeleteDismissed       -> _uiState.update { it.copy(showDeleteDialog = false) }
            is BudgetManualEvent.OnCategorySelected    -> _uiState.update { it.copy(selectedCategory = event.category, showCategorySheet = false) }
            is BudgetManualEvent.CategoryClicked       -> _uiState.update { it.copy(showCategorySheet = true) }
            is BudgetManualEvent.DismissCategorySheet  -> _uiState.update { it.copy(showCategorySheet = false) }
            is BudgetManualEvent.DismissError          -> _uiState.update { it.copy(error = null) }
            is BudgetManualEvent.ClearCategory         -> _uiState.update { it.copy(selectedCategory = null) }
        }
    }

    private fun saveBudget() {
        val state = _uiState.value
        if (state.selectedCategory == null) {
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
                    id = state.id,
                    userId = userId,
                    categoryId = state.selectedCategory.id,
                    amountLimit = amountLimit,
                    month = state.month,
                    year = state.year,
                    createdAt = if (state.isEditMode) state.createdAt else now,
                    updatedAt = now,
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