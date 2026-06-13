package com.example.mymoney.presentation.viewmodel.saving.saving_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.usecase.AddSavingRecordUseCase
import com.example.mymoney.domain.usecase.GetSavingGoalDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavingDetailViewModel (
    private val savingRecordRepository: SavingRecordRepository,
    private val getSavingGoalDetailUseCase: GetSavingGoalDetailUseCase,
    private val addSavingRecordUseCase: AddSavingRecordUseCase,
    private val goalId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(SavingDetailUiState())
    val uiState: StateFlow<SavingDetailUiState> = _uiState.asStateFlow()

    init {
        loadSavingRecords()
    }

    private fun loadSavingRecords() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = getSavingGoalDetailUseCase(goalId)
                _uiState.update { it.copy(isLoading = false, detail = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Tải thất bại: ${e.message}") }
            }
        }
    }

    fun onEvent(event: SavingDetailEvent) {
        when (event) {
            is SavingDetailEvent.SaveRecord -> viewModelScope.launch {
                addSavingRecordUseCase(event.record)
                loadSavingRecords()
            }

            is SavingDetailEvent.DeleteRecord -> viewModelScope.launch {
                savingRecordRepository.deleteRecord(event.recordId)
                loadSavingRecords()
            }
        }
    }
}