package com.example.mymoney.presentation.viewmodel.saving.saving_detail

import com.example.mymoney.domain.model.SavingGoalDetailModel

data class SavingDetailUiState(
    val isLoading: Boolean = false,
    val detail: SavingGoalDetailModel? = null,
    val error: String? = null
)

sealed interface SavingDetailEvent {
    data class DeleteRecord(val recordId: Long) : SavingDetailEvent
}