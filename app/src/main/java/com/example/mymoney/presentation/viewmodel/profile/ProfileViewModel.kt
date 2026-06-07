package com.example.mymoney.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val settingPreferences: SettingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<ProfileNavEvent>()
    val navEvent: SharedFlow<ProfileNavEvent> = _navEvent.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val username = authRepository.getCurrentUsername() ?: ""
            _uiState.update { it.copy(username = username, isLoading = false) }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.UpdateUsername          -> updateUsername(event.newUsername)
            is ProfileEvent.ToggleEditUsername      ->_uiState.update {it.copy(isEditingUsername = !it.isEditingUsername,newUsername = it.username)}
            is ProfileEvent.UsernameChanged         ->_uiState.update {it.copy(newUsername = event.value)}
            is ProfileEvent.Password                -> handlePasswordEvent(event.event)
            is ProfileEvent.UpdatePassword          -> updatePassword(_uiState.value.password)
            is ProfileEvent.DeleteAccountConfirmed  -> deleteAccount()
            is ProfileEvent.DeleteAccountClicked    -> _uiState.update { it.copy(showDeleteConfirmDialog = true) }
            is ProfileEvent.DeleteAccountDismissed  -> _uiState.update { it.copy(showDeleteConfirmDialog = false) }
            is ProfileEvent.DismissSnackbar         -> _uiState.update { it.copy(error = null, successMessage = null) }
            is ProfileEvent.ToggleExpanded          -> _uiState.update { it.copy(isExpanded = !it.isExpanded) }
        }
    }

    private fun updateUsername(newUsername: String) {
        if (newUsername.isBlank()) {
            _uiState.update { it.copy(error = "Tên không được để trống") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authRepository.updateUsername(newUsername)
                settingPreferences.saveUsername(newUsername)
                _uiState.update { it.copy(
                    username = newUsername,
                    newUsername = newUsername,
                    isEditingUsername = false,
                    successMessage = "Cập nhật tên thành công",
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi: ${e.message}", isLoading = false) }
            }
        }
    }

    private fun updatePassword(password: PasswordState) {
        val oldPassword = password.old
        val newPassword = password.new
        val confirmPassword = password.confirm

        if (oldPassword.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập mật khẩu cũ") }
            return
        }

        if (newPassword.length < 6) {
            _uiState.update { it.copy(error = "Mật khẩu mới phải từ 6 ký tự") }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(error = "Mật khẩu xác nhận không khớp") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                /**
                 * Supabase mobile SDK thường không yêu cầu mật khẩu cũ
                 * khi cập nhật password bằng `updateUser` nếu user đang đăng nhập.
                 *
                 * Tuy nhiên, vì lý do bảo mật, nhiều ứng dụng production
                 * vẫn nên yêu cầu nhập lại mật khẩu cũ để xác thực.
                 */
                authRepository.updatePassword(newPassword)
                authRepository.signOut()
                // Xóa local cache (nếu có)
                settingPreferences.clearUserId()
                settingPreferences.clearUsername()
                _navEvent.emit(ProfileNavEvent.NavigateToSignIn)
                _uiState.update { it.copy(password = PasswordState(), isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi: ${e.message}", isLoading = false) }
            }
        }
    }

    private fun handlePasswordEvent(event: PasswordEvent) {
        _uiState.update { state ->
            when (event) {
                is PasswordEvent.OldChanged              -> state.copy(password = state.password.copy(old = event.value))
                is PasswordEvent.NewChanged              -> state.copy(password = state.password.copy(new = event.value))
                is PasswordEvent.ConfirmChanged          -> state.copy(password = state.password.copy(confirm = event.value))
                is PasswordEvent.ToggleOldVisibility     -> state.copy(password = state.password.copy(oldVisible = !state.password.oldVisible))
                is PasswordEvent.ToggleNewVisibility     -> state.copy(password = state.password.copy(newVisible = !state.password.newVisible))
                is PasswordEvent.ToggleConfirmVisibility -> state.copy(password = state.password.copy(confirmVisible = !state.password.confirmVisible))
            }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteConfirmDialog = false) }
            try {
                authRepository.deleteAccount()
                // Xóa local data
                settingPreferences.clearUserId()
                settingPreferences.clearUsername()
                _navEvent.emit(ProfileNavEvent.NavigateToSignIn)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi: ${e.message}", isLoading = false) }
            }
        }
    }
}
