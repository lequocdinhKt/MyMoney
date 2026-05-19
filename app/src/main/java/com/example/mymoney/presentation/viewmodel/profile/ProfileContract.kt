package com.example.mymoney.presentation.viewmodel.profile

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho ProfileScreen
// ─────────────────────────────────────────────────────────────────────────────

data class PasswordState(
    val old: String = "",
    val new: String = "",
    val confirm: String = "",
    val oldVisible: Boolean = false,
    val newVisible: Boolean = false,
    val confirmVisible: Boolean = false
)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val username: String = "", // username hiện tại (đã lưu trong hệ thống)
    val isEditingUsername: Boolean = false,   // trạng thái đang chỉnh sửa username hay không
    val newUsername: String = "", // username tạm thời khi user đang chỉnh sửa
    val password: PasswordState = PasswordState(),
    val error: String? = null,
    val successMessage: String? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val isExpanded: Boolean = false  // mở/đóng section đổi mật khẩu
)

sealed interface ProfileEvent {
    data class UpdateUsername(val newUsername: String) : ProfileEvent // submit cập nhật username
    data class UsernameChanged(val value: String) : ProfileEvent  // thay đổi text username khi đang edit
    data class Password(val event: PasswordEvent) : ProfileEvent
    data object DeleteAccountClicked : ProfileEvent
    data object DeleteAccountConfirmed : ProfileEvent
    data object DeleteAccountDismissed : ProfileEvent
    data object DismissSnackbar : ProfileEvent
    data object UpdatePassword: ProfileEvent // submit đổi mật khẩu
    data object ToggleEditUsername : ProfileEvent  // bật/tắt chế độ edit username
    data object ToggleExpanded : ProfileEvent // expand / collapse section đổi mật khẩu
}

sealed interface ProfileNavEvent {
    data object NavigateBack : ProfileNavEvent // quay lại màn trước
    data object NavigateToSignIn : ProfileNavEvent // chuyển sang màn đăng nhập (logout / delete account / đổi password)
}

sealed interface PasswordEvent {
    data class OldChanged(val value: String) : PasswordEvent
    data class NewChanged(val value: String) : PasswordEvent
    data class ConfirmChanged(val value: String) : PasswordEvent

    data object ToggleOldVisibility : PasswordEvent
    data object ToggleNewVisibility : PasswordEvent
    data object ToggleConfirmVisibility : PasswordEvent
}
