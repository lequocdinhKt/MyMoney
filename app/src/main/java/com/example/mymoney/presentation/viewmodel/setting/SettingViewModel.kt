package com.example.mymoney.presentation.viewmodel.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Cài đặt.
 */
class SettingViewModel (
    private val settingPreferences: SettingPreferences
): ViewModel() {
    /** State duy nhất mà UI observe */
    val uiState: StateFlow<SettingUiState> = settingPreferences.isThousandSeparatorEnabled
        .map { enabled -> SettingUiState(
            isThousandSeparatorEnabled = enabled
        )}.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingUiState(
                isThousandSeparatorEnabled = true
            )
        )


    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingViewModel(
                        SettingPreferences(context.applicationContext)
                    ) as T
                }
            }
    }
    /** Xử lý sự kiện từ UI */
    fun onEvent(event: SettingEvent) {
        when (event) {
            is SettingEvent.ToggleThousandSeparator -> {
                // Lưu vào Datastore
                viewModelScope.launch {
                    settingPreferences.setThousandSeparatorEnabled(event.enabled)
                }
            }
        }
    }
}
