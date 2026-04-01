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
    val isThousandSeparatorEnabled: Boolean = true,
    val username: String = ""  // Tên hiển thị lấy từ Supabase Auth metadata
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
    data object SignOut : SettingEvent
}

/**
 * Navigation side-effect phát qua SharedFlow.
 * UI collect 1 lần qua LaunchedEffect — không lưu trong UiState.
 */
sealed interface SettingNavEvent {
    /** Đăng xuất thành công → về màn hình Sign In */
    data object NavigateToSignIn : SettingNavEvent
}
