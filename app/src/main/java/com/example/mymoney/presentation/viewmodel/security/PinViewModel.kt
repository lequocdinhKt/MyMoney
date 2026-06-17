package com.example.mymoney.presentation.viewmodel.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinViewModel(
    private val settingPreferences: SettingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val currentPin = settingPreferences.pinCode.first()
            val isBiometric = settingPreferences.isBiometricEnabled.first()
            _uiState.update { 
                it.copy(
                    currentPin = currentPin,
                    isBiometricEnabled = isBiometric,
                    step = if (currentPin != null) PinStep.ENTER_CURRENT_PIN else PinStep.ENTER_NEW_PIN
                )
            }
        }
    }

    fun onEvent(event: PinEvent) {
        when (event) {
            is PinEvent.OnNumberClick -> handleNumberClick(event.number)
            is PinEvent.OnDeleteClick -> handleDeleteClick()
            is PinEvent.OnClearClick -> handleClearClick()
            is PinEvent.OnToggleBiometric -> handleToggleBiometric(event.enabled)
            is PinEvent.OnResetState -> _uiState.update { it.copy(isSuccess = false, errorMessage = null) }
        }
    }

    private fun handleNumberClick(number: String) {
        val currentState = _uiState.value
        if (currentState.enteredPin.length >= 6) return

        val newPin = currentState.enteredPin + number
        _uiState.update { it.copy(enteredPin = newPin, errorMessage = null) }

        if (newPin.length == 6) {
            processPinCompletion(newPin)
        }
    }

    private fun processPinCompletion(pin: String) {
        val currentState = _uiState.value
        when (currentState.step) {
            PinStep.ENTER_CURRENT_PIN -> {
                if (pin == currentState.currentPin) {
                    _uiState.update { 
                        it.copy(
                            step = PinStep.ENTER_NEW_PIN,
                            enteredPin = ""
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            enteredPin = "",
                            errorMessage = "Mã PIN hiện tại không đúng"
                        )
                    }
                }
            }
            PinStep.ENTER_NEW_PIN -> {
                _uiState.update { 
                    it.copy(
                        step = PinStep.CONFIRM_NEW_PIN,
                        firstNewPin = pin,
                        enteredPin = ""
                    )
                }
            }
            PinStep.CONFIRM_NEW_PIN -> {
                if (pin == currentState.firstNewPin) {
                    savePin(pin)
                } else {
                    _uiState.update { 
                        it.copy(
                            enteredPin = "",
                            errorMessage = "Mã PIN xác nhận không khớp"
                        )
                    }
                }
            }
        }
    }

    private fun handleDeleteClick() {
        _uiState.update { 
            if (it.enteredPin.isNotEmpty()) {
                it.copy(enteredPin = it.enteredPin.dropLast(1))
            } else it
        }
    }

    private fun handleClearClick() {
        _uiState.update { it.copy(enteredPin = "") }
    }

    private fun handleToggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingPreferences.setBiometricEnabled(enabled)
            _uiState.update { it.copy(isBiometricEnabled = enabled) }
        }
    }

    private fun savePin(pin: String) {
        viewModelScope.launch {
            settingPreferences.savePinCode(pin)
            _uiState.update { it.copy(isSuccess = true, currentPin = pin) }
        }
    }
}
