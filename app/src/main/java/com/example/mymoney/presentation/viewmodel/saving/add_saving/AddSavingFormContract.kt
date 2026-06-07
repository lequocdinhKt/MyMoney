package com.example.mymoney.presentation.viewmodel.saving.add_saving

import java.time.LocalDate

data class AddSavingFormUiState(
    val isLoading: Boolean = false,
    val mode: SavingMode = SavingMode.ONE_TIME,
    val title: String = "",
    val amount: String = "",
    val targetDate: LocalDate = LocalDate.now().plusWeeks(1),  // One-time
    val recurringType: RecurringType = RecurringType.MONTHLY, // Recurring
    val isSaved : Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false, // Hiện dialog chọn lịch
    val showCurrencySheet: Boolean = false,
    val currency: String = "VNĐ"
)

enum class SavingMode {
    ONE_TIME,
    RECURRING
}

enum class RecurringType {
    WEEKLY,
    MONTHLY
}

sealed interface AddSavingEvent {
    data class OnTitleChanged(val title: String) : AddSavingEvent
    data class OnAmountChanged(val value: String) : AddSavingEvent
    data class OnTargetDateSelected(val date: LocalDate) : AddSavingEvent
    data class OnRecurringTypeSelected(val type: RecurringType) : AddSavingEvent
    data class OnModeChanged(val mode: SavingMode) : AddSavingEvent
    data class OnCurrencyChanged(val currency: String) : AddSavingEvent
    data object OnShowDatePicker : AddSavingEvent
    data object OnDismissDatePicker : AddSavingEvent
    data object OnShowCurrencySheet : AddSavingEvent
    data object OnDismissCurrencySheet : AddSavingEvent
    data object OnSave : AddSavingEvent
}