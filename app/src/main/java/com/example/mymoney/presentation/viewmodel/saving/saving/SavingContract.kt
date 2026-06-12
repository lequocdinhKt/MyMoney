package com.example.mymoney.presentation.viewmodel.saving.saving

import com.example.mymoney.domain.model.SavingGoalModel

/**
 * Trạng thái giao diện của màn hình Tiết kiệm.
 */
data class SavingUiState(
    val isLoading: Boolean = false,
    val savingGoals: List<SavingGoalItem> = emptyList(),
    val isShowCompletedEnabled: Boolean = true, // Có hiện mục tiêu đã hoàn thành ko - mặc định là có
    val selectedType: SavingType = SavingType.ONE_TIME
)

data class SavingGoalItem(
    val goal: SavingGoalModel,
    val currentAmount: Double
)

enum class SavingType {
    ONE_TIME,
    RECURRING
}

/**
 * Sự kiện người dùng gửi từ SavingScreen lên ViewModel.
 */
sealed interface SavingEvent {
    data class ToggleShowCompleted(val enabled: Boolean) : SavingEvent
    data class SaveGoal(val goal: SavingGoalModel) : SavingEvent
    data class DeleteGoal(val id: Long) : SavingEvent
    data class SelectedType(val type: SavingType) : SavingEvent
}
