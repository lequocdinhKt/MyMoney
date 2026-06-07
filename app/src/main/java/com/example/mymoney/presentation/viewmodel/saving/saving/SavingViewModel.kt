package com.example.mymoney.presentation.viewmodel.saving.saving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.domain.repository.SavingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SavingViewModel(
    private val settingPreferences: SettingPreferences,
    private val userId: String,
    private val savingRepository: SavingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingUiState())
    val uiState: StateFlow<SavingUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
        loadSavingGoals()
    }

    private fun loadSavingGoals() {
        savingRepository.getSavingGoals(userId)
            .onEach { list -> _uiState.value = _uiState.value.copy(savingGoals = list, isLoading = false) }
            .launchIn(viewModelScope)
    }

    private fun observeSettings() {
        settingPreferences.isShowCompletedEnabled
            .onEach { value ->
                _uiState.value = _uiState.value.copy(
                    isShowCompletedEnabled = value
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SavingEvent) {
        when (event) {
            is SavingEvent.ToggleShowCompleted -> {
                viewModelScope.launch {
                    settingPreferences.setShowCompletedEnabled(event.enabled)
                }
            }

            is SavingEvent.SelectedType -> {
                val current = _uiState.value.selectedType

                val newType = if (current == event.type) {
                    SavingType.ONE_TIME
                } else {
                    event.type
                }
                _uiState.value = _uiState.value.copy(selectedType = newType)
            }
            is SavingEvent.SaveGoal -> viewModelScope.launch {
                savingRepository.addSavingGoal(event.goal)
            }
            is SavingEvent.DeleteGoal -> viewModelScope.launch {
                savingRepository.deleteSavingGoal(event.id)
            }
        }
    }
}
