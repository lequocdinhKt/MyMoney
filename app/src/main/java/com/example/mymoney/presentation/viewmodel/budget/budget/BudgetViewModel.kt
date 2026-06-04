package com.example.mymoney.presentation.viewmodel.budget.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Ngân sách.
 */
class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        val cal = Calendar.getInstance()
        loadBudgets(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    private fun loadBudgets(month: Int, year: Int) {
        budgetRepository.getBudgets(userId, month, year)
            .onEach { list -> _uiState.value = _uiState.value.copy(budgets = list, isLoading = false) }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: BudgetEvent) {
        when (event) {
            is BudgetEvent.SaveBudget -> viewModelScope.launch {
                budgetRepository.saveBudget(event.budget)
            }
            is BudgetEvent.DeleteBudget -> viewModelScope.launch {
                budgetRepository.deleteBudget(event.id)
            }
            is BudgetEvent.LoadMonth -> loadBudgets(event.month, event.year)
        }
    }
}
