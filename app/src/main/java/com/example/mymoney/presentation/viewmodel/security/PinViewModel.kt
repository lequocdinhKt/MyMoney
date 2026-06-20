package com.example.mymoney.presentation.viewmodel.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinViewModel(
    private val settingPreferences: SettingPreferences,
    private val authRepository: AuthRepository,
    private val isSetupFlow: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        startLockoutTimer()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val currentPin = settingPreferences.pinCode.first()
            val isBiometric = settingPreferences.isBiometricEnabled.first()
            val retryCount = settingPreferences.pinRetryCount.first()
            val lockUntil = settingPreferences.pinLockedUntil.first()

            _uiState.update { 
                it.copy(
                    currentPin = currentPin,
                    isBiometricEnabled = isBiometric,
                    retryCount = retryCount,
                    lockUntil = lockUntil,
                    step = when {
                        isSetupFlow -> {
                            if (currentPin != null) PinStep.ENTER_CURRENT_PIN else PinStep.ENTER_NEW_PIN
                        }
                        else -> PinStep.UNLOCK
                    }
                )
            }
        }
    }

    private fun startLockoutTimer() {
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val currentLockUntil = _uiState.value.lockUntil
                if (currentLockUntil > 0 && now >= currentLockUntil) {
                    resetLockout()
                }
                delay(1000)
            }
        }
    }

    private suspend fun resetLockout() {
        settingPreferences.setPinRetryCount(0)
        settingPreferences.setPinLockedUntil(0)
        _uiState.update { it.copy(retryCount = 0, lockUntil = 0, errorMessage = null) }
    }

    fun onEvent(event: PinEvent) {
        when (event) {
            is PinEvent.OnNumberClick -> handleNumberClick(event.number)
            is PinEvent.OnDeleteClick -> handleDeleteClick()
            is PinEvent.OnClearClick -> handleClearClick()
            is PinEvent.OnToggleBiometric -> handleToggleBiometric(event.enabled)
            is PinEvent.OnResetState -> _uiState.update { it.copy(isSuccess = false, errorMessage = null) }
            is PinEvent.OnForgotPinClick -> _uiState.update { it.copy(isForgotPinDialogVisible = true) }
            is PinEvent.OnConfirmForgotPin -> handleForgotPin()
            is PinEvent.OnDismissForgotPin -> _uiState.update { it.copy(isForgotPinDialogVisible = false) }
            is PinEvent.OnBiometricSuccess -> handleSuccessUnlock()
        }
    }

    private fun handleNumberClick(number: String) {
        val currentState = _uiState.value
        // Kiểm tra khóa
        if (currentState.lockUntil > System.currentTimeMillis()) return
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
            PinStep.UNLOCK -> {
                if (pin == currentState.currentPin) {
                    handleSuccessUnlock()
                } else {
                    handleFailedAttempt()
                }
            }
            PinStep.ENTER_CURRENT_PIN -> {
                if (pin == currentState.currentPin) {
                    _uiState.update { 
                        it.copy(
                            step = PinStep.ENTER_NEW_PIN,
                            enteredPin = "",
                            retryCount = 0
                        )
                    }
                    viewModelScope.launch { settingPreferences.setPinRetryCount(0) }
                } else {
                    handleFailedAttempt()
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

    private fun handleSuccessUnlock() {
        viewModelScope.launch {
            resetLockout()
            _uiState.update { it.copy(isSuccess = true) }
        }
    }

    private fun handleFailedAttempt() {
        viewModelScope.launch {
            val newCount = _uiState.value.retryCount + 1
            var lockUntil = 0L
            var errorMsg = "Mã PIN không đúng ($newCount/5)"

            if (newCount >= 5) {
                lockUntil = System.currentTimeMillis() + (5 * 60 * 1000) // 5 phút
                errorMsg = "Nhập sai quá nhiều lần. Thử lại sau 5 phút."
            }

            settingPreferences.setPinRetryCount(newCount)
            settingPreferences.setPinLockedUntil(lockUntil)

            _uiState.update { 
                it.copy(
                    enteredPin = "",
                    retryCount = newCount,
                    lockUntil = lockUntil,
                    errorMessage = errorMsg
                )
            }
        }
    }

    private fun handleForgotPin() {
        viewModelScope.launch {
            authRepository.signOut()
            settingPreferences.clearUserId()
            settingPreferences.clearUsername()
            settingPreferences.savePinCode(null)
            settingPreferences.setBiometricEnabled(false)
            resetLockout()
            _uiState.update { it.copy(isSuccess = true, isForgotPinDialogVisible = false) }
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
