package com.example.mymoney.presentation.viewmodel.security

data class PinUiState(
    val step: PinStep = PinStep.ENTER_NEW_PIN,
    val enteredPin: String = "",
    val firstNewPin: String = "",
    val currentPin: String? = null,
    val isBiometricEnabled: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

enum class PinStep {
    ENTER_CURRENT_PIN, // Khi chỉnh sửa, cần nhập PIN cũ
    ENTER_NEW_PIN,     // Nhập PIN mới lần 1
    CONFIRM_NEW_PIN    // Nhập PIN mới lần 2 để xác nhận
}

sealed interface PinEvent {
    data class OnNumberClick(val number: String) : PinEvent
    data object OnDeleteClick : PinEvent
    data object OnClearClick : PinEvent
    data class OnToggleBiometric(val enabled: Boolean) : PinEvent
    data object OnResetState : PinEvent
}
