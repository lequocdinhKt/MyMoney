package com.example.mymoney.presentation.viewmodel.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.repository.AuthRepositoryImpl
import com.example.mymoney.domain.repository.AuthRepository // interface: getCurrentUsername()
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingNavEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Cài đặt.
 *
 * Nguồn dữ liệu:
 *   - [SettingPreferences] : isThousandSeparatorEnabled (DataStore)
 *   - [AuthRepository]     : username lấy từ Supabase Auth metadata
 */
class SettingViewModel(
    private val settingPreferences: SettingPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    // sự kiện điều hướng cho Navigation Component
    val uiState: StateFlow<SettingUiState> =
        combine(
            settingPreferences.isThousandSeparatorEnabled,
            settingPreferences.currentUsername   // đọc thẳng từ DataStore, không cần network
        ) { enabled, username ->
            SettingUiState(
                isThousandSeparatorEnabled = enabled,
                username = username
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingUiState()
        )

    // sự kiện điều hướng cho Navigation Component
    private val _navEvent = MutableSharedFlow<SettingNavEvent>()
    val navEvent: SharedFlow<SettingNavEvent> = _navEvent.asSharedFlow()


    /** Xử lý sự kiện từ UI */
    fun onEvent(event: SettingEvent) {
        when (event) {
            is SettingEvent.ToggleThousandSeparator -> {
                viewModelScope.launch {
                    settingPreferences.setThousandSeparatorEnabled(event.enabled)
                }
            }
            is SettingEvent.SignOut -> {
                viewModelScope.launch {
                    authRepository.signOut()
                    settingPreferences.clearUserId()
                    settingPreferences.clearUsername()   // xóa username cùng lúc
                    _navEvent.emit(SettingNavEvent.NavigateToSignIn)
                }
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingViewModel(
                        settingPreferences = SettingPreferences(context.applicationContext),
                        authRepository = AuthRepositoryImpl()
                    ) as T
                }
            }
    }
}
