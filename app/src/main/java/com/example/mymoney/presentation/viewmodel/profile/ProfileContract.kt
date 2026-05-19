package com.example.mymoney.presentation.viewmodel.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val isEditingUsername: Boolean = false,
    val newUsername: String = "",
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
    val successMessage: String? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val oldPasswordVisible: Boolean = false,
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isExpanded: Boolean = false
)

sealed interface ProfileEvent {
    data object ToggleEditUsername : ProfileEvent
    data class UsernameChanged(val value: String) : ProfileEvent
    data class UpdateUsername(val newUsername: String) : ProfileEvent
    data class UpdatePassword(val oldPassword: String, val newPassword: String, val confirmPassword: String) : ProfileEvent
    data class OldPasswordChanged(val value: String) : ProfileEvent
    data class NewPasswordChanged(val value: String) : ProfileEvent
    data class ConfirmPasswordChanged(val value: String) : ProfileEvent
    data object DeleteAccountClicked : ProfileEvent
    data object DeleteAccountConfirmed : ProfileEvent
    data object DeleteAccountDismissed : ProfileEvent
    data object DismissSnackbar : ProfileEvent
    data object ToggleOldPasswordVisibility : ProfileEvent
    data object ToggleNewPasswordVisibility : ProfileEvent
    data object ToggleConfirmPasswordVisibility : ProfileEvent
    data object ToggleExpanded : ProfileEvent
}

sealed interface ProfileNavEvent {
    data object NavigateBack : ProfileNavEvent
    data object NavigateToSignIn : ProfileNavEvent

}
