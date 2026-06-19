package com.example.mymoney.presentation.viewmodel.saving.saving_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.usecase.DeleteSavingRecordUseCase
import com.example.mymoney.domain.usecase.GetSavingGoalDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavingDetailViewModel (
    private val deleteSavingRecordUseCase: DeleteSavingRecordUseCase,
    private val getSavingGoalDetailUseCase: GetSavingGoalDetailUseCase,
    private val goalId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavingDetailUiState())
    val uiState: StateFlow<SavingDetailUiState> = _uiState.asStateFlow()

    init {
        observeSavingRecords()
    }

    private fun observeSavingRecords() {
        viewModelScope.launch {
            getSavingGoalDetailUseCase(goalId)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = "Tải thất bại: ${e.message}") } }
                .collect { result ->
                    _uiState.update { it.copy(isLoading = false, detail = result, error = null) }
                }
        }
    }

    fun onEvent(event: SavingDetailEvent) {
        when (event) {
            is SavingDetailEvent.DeleteRecord -> viewModelScope.launch {
                try {
                    deleteSavingRecordUseCase(event.recordId)
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Xóa thất bại: ${e.message}") }
                }
            }
        }
    }
}