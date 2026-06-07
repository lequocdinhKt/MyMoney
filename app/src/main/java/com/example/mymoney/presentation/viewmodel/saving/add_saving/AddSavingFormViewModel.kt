package com.example.mymoney.presentation.viewmodel.saving.add_saving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.domain.model.SavingGoalModel
import com.example.mymoney.domain.model.SavingType
import com.example.mymoney.domain.usecase.AddSavingGoalUserCase
import com.example.mymoney.domain.usecase.MoneyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

class AddSavingFormViewModel(
    private val userId: String,
    private val addSavingUseCase: AddSavingGoalUserCase,
    private val settingPreferences: SettingPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddSavingFormUiState())
    val uiState: StateFlow<AddSavingFormUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddSavingEvent) {
        when (event) {
            is AddSavingEvent.OnTitleChanged          -> _uiState.update { it.copy(title = event.title) }
            is AddSavingEvent.OnAmountChanged         -> {
                viewModelScope.launch {
                    val useGrouping = settingPreferences.isThousandSeparatorEnabled.first()
                    val formatted = MoneyFormatter.formatInput(event.value, useGrouping)
                    _uiState.update { it.copy(amount = formatted, error = null) }
                }
            }
            is AddSavingEvent.OnTargetDateSelected    -> _uiState.update { it.copy(targetDate = event.date, showDatePicker = false) }
            is AddSavingEvent.OnRecurringTypeSelected -> _uiState.update { it.copy(recurringType = event.type) }
            is AddSavingEvent.OnCurrencyChanged       -> _uiState.update { it.copy(currency = event.currency, showCurrencySheet = false) }
            is AddSavingEvent.OnModeChanged           -> {
                _uiState.update { state ->
                    when (event.mode) {
                        SavingMode.ONE_TIME -> {
                            state.copy(
                                mode = SavingMode.ONE_TIME,
                                targetDate = state.targetDate
                            )
                        }

                        SavingMode.RECURRING -> {
                            state.copy(mode = SavingMode.RECURRING)
                        }
                    }
                }
            }
            is AddSavingEvent.OnShowDatePicker        -> _uiState.update { it.copy(showDatePicker = true) }
            is AddSavingEvent.OnDismissDatePicker     -> _uiState.update { it.copy(showDatePicker = false) }
            is AddSavingEvent.OnShowCurrencySheet     -> _uiState.update { it.copy(showCurrencySheet = true) }
            is AddSavingEvent.OnDismissCurrencySheet  -> _uiState.update { it.copy(showCurrencySheet = false) }
            is AddSavingEvent.OnSave                  -> saveSavingGoal()
        }
    }

    private fun saveSavingGoal() {
        val state = _uiState.value

        val amount = state.amount.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0

        if (state.title.isBlank()) {
            _uiState.update {
                it.copy(error = "Vui lòng nhập tiêu đề")
            }
            return
        }

        if (amount <= 0.0) {
            _uiState.update {
                it.copy(error = "Số tiền không hợp lệ")
            }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val savingType = when (state.mode) {
                    SavingMode.ONE_TIME -> SavingType.ONE_TIME


                    SavingMode.RECURRING -> when (state.recurringType) {
                        RecurringType.WEEKLY -> SavingType.WEEKLY
                        RecurringType.MONTHLY -> SavingType.MONTHLY
                    }
                }

                val targetDateMillis = if (savingType == SavingType.ONE_TIME) {
                    (state.targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                } else {
                    null
                }

                val savingGoal = SavingGoalModel(
                    userId = userId,
                    title = state.title.trim(),
                    currency = state.currency,
                    targetAmount = amount,
                    savingType = savingType,
                    targetDate = targetDateMillis
                )

                addSavingUseCase(savingGoal)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Đã xảy ra lỗi"
                )
            }
        }
    }
}