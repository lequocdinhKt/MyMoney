package com.example.mymoney.presentation.viewmodel.setting.setting

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingUiState(
    val isLoading: Boolean = false,
    val isThousandSeparatorEnabled: Boolean = true,
    val username: String = "",

    // ── Theme ──
    val selectedTheme: ThemeMode = ThemeMode.SYSTEM, // Chế độ có được chọn ko
    val showThemeSheet: Boolean = false, // Hiện hay ẩn màn hình lựa chọn chế độ

    // ── Currency ──
    val selectedCurrency: CurrencyMode = CurrencyMode.VND,
    val showCurrencySheet: Boolean = false,

    // ── Number Format ──
    val selectedNumberFormat: NumberFormat = NumberFormat.COMMA,
    val showNumberFormat: Boolean = false,

    // ── Backup ──
    val isBackingUp: Boolean = false,           // đang upload → hiện loading
    val backupResultMessage: String? = null,    // kết quả: "✅ Đã sao lưu 12 giao dịch" hoặc lỗi
    val showBackupConfirmDialog: Boolean = false
)

sealed class SettingItem {
    data class SettingNavigation(
        val title: String,
        val icon: ImageVector,
        val route: String
    ) : SettingItem()

    data class SettingToggle(
        val title: String,
        val isChecked: Boolean = false,
    ) : SettingItem()
}

sealed interface SettingEvent {
    data class ToggleThousandSeparator(val enabled: Boolean) : SettingEvent
    data object SignOut : SettingEvent
    data object BackupToSupabaseClicked : SettingEvent   // nhấn "Dữ liệu và sao lưu"
    data object BackupConfirmed : SettingEvent            // xác nhận trong dialog
    data object BackupDismissed : SettingEvent            // đóng dialog
    data object DismissBackupResult : SettingEvent        // đóng snackbar/thông báo kết quả

    // ── Theme ──
    data object ThemeClicked : SettingEvent               // nhấn "Giao diện"

    data class ThemeSelected(val mode: ThemeMode) : SettingEvent // xác nhận chế độ được chọn

    data object ThemeDismissed : SettingEvent             // đóng ô lựa chọn giao diện

    // ── Currency ──
    data object CurrencyClicked : SettingEvent
    data class CurrencySelected(val currency: CurrencyMode) : SettingEvent
    data object CurrencyDismissed : SettingEvent

    // ── Number Format ──
    data object NumberFormatClicked : SettingEvent
    data class NumberFormatSelected(val numberformat: NumberFormat) : SettingEvent
    data object NumberFormatDismissed : SettingEvent
}

sealed interface SettingNavEvent {
    data object NavigateToSignIn : SettingNavEvent
}
