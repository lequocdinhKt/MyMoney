package com.example.mymoney.presentation.viewmodel.setting.setting

import androidx.compose.ui.graphics.vector.ImageVector

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event cho SettingScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trạng thái giao diện của màn hình Cài đặt.
 * Dùng data class bất biến – cập nhật bằng copy().
 */
data class SettingUiState(
    val isLoading: Boolean = false,
    val isThousandSeparatorEnabled: Boolean = true // Lưu trạng thái của switch "Dấu phân cách hàng nghìn"
)

sealed class SettingItem {
    data class SettingNavigation (
        val title: String,
        val icon: ImageVector,
        val route: String // dùng cho Navigation sau này
        ) : SettingItem()

    data class SettingToggle (
        val title: String,
        val isChecked: Boolean = false,
    ) : SettingItem()
}

/**
 * Sự kiện người dùng gửi từ SettingScreen lên ViewModel.
 */
sealed interface SettingEvent {
    data class ToggleThousandSeparator(val enabled: Boolean) : SettingEvent
}
