package com.example.mymoney.presentation.viewmodel.setting

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.SupabaseSyncRepository
import com.example.mymoney.domain.repository.AuthRepository
import com.example.mymoney.presentation.viewmodel.setting.setting.CurrencyMode
import com.example.mymoney.presentation.viewmodel.setting.setting.NumberFormat
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingNavEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Internal state cho backup — tách riêng để combine dễ hơn */
private data class BackupViewState(
    val isBackingUp: Boolean = false,
    val showDialog: Boolean = false,
    val resultMsg: String? = null
)

/** Internal state cho UI-only (theme, currency, number format) */
private data class SettingExtrasState(
    val showThemeSheet: Boolean = false,
    val selectedCurrency: CurrencyMode = CurrencyMode.VND,
    val showCurrencySheet: Boolean = false,
    val selectedNumberFormat: NumberFormat = NumberFormat.DOT,
//    val showNumberFormat: Boolean = false
)

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Cài đặt.
 */
class SettingViewModel(
    private val settingPreferences: SettingPreferences,
    private val authRepository: AuthRepository,
    private val db: AppDatabase
) : ViewModel() {

    private val TAG = "SettingViewModel"
    private val syncRepository = SupabaseSyncRepository(db)

    // ── UI state: merge DataStore flows + backup state + UI extras ──
    private val _backupState = MutableStateFlow(BackupViewState())
    private val _extrasState = MutableStateFlow(SettingExtrasState())

    init {
        // Load numberFormat từ DataStore vào _extrasState khi ViewModel khởi động
        viewModelScope.launch {
            settingPreferences.numberFormat.collect { savedFormat ->
                _extrasState.update { it.copy(selectedNumberFormat = savedFormat) }
            }
        }
    }

    val uiState: StateFlow<SettingUiState> =
        combine(
            settingPreferences.isThousandSeparatorEnabled,
            settingPreferences.currentUsername,
            settingPreferences.themeMode,
            _backupState,
            _extrasState
        ) { enabled, username, themeMode, backup, extras ->
            SettingUiState(
                isThousandSeparatorEnabled = enabled,
                username                   = username ?: "",
                isBackingUp                = backup.isBackingUp,
                showBackupConfirmDialog    = backup.showDialog,
                backupResultMessage        = backup.resultMsg,
                selectedTheme              = themeMode,
                showThemeSheet             = extras.showThemeSheet,
                selectedCurrency           = extras.selectedCurrency,
                showCurrencySheet          = extras.showCurrencySheet,
//                selectedNumberFormat       = extras.selectedNumberFormat,
//                showNumberFormat           = extras.showNumberFormat
            )
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5000),
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
            // Nhấn item "Dữ liệu và sao lưu" → hiện dialog xác nhận
            is SettingEvent.BackupToSupabaseClicked -> {
                _backupState.update { it.copy(showDialog = true) }
            }
            // Xác nhận trong dialog → bắt đầu backup
            is SettingEvent.BackupConfirmed -> {
                _backupState.update { it.copy(showDialog = false) }
                startBackup()
            }
            // Đóng dialog không làm gì
            is SettingEvent.BackupDismissed -> {
                _backupState.update { it.copy(showDialog = false) }
            }
            // Đóng thông báo kết quả
            is SettingEvent.DismissBackupResult -> {
                _backupState.update { it.copy(resultMsg = null) }
            }
            // ── Theme ──
            is SettingEvent.ThemeClicked -> {
                _extrasState.update { it.copy(showThemeSheet = true) }
            }
            is SettingEvent.ThemeSelected -> {
                viewModelScope.launch {
                    settingPreferences.setThemeMode(event.mode)
                    _extrasState.update { it.copy(showThemeSheet = false) }
                }
            }
            is SettingEvent.ThemeDismissed -> {
                _extrasState.update { it.copy(showThemeSheet = false) }
            }
            // ── PIN ──
            is SettingEvent.PinClicked -> {
                viewModelScope.launch {
                    _navEvent.emit(SettingNavEvent.NavigateToPinSetup)
                }
            }
            // ── Currency ──
            is SettingEvent.CurrencyClicked -> {
                _extrasState.update { it.copy(showCurrencySheet = true) }
            }
            is SettingEvent.CurrencySelected -> {
                _extrasState.update { it.copy(selectedCurrency = event.currency, showCurrencySheet = false) }
            }
            is SettingEvent.CurrencyDismissed -> {
                _extrasState.update { it.copy(showCurrencySheet = false) }
            }
            // ── Number Format ──
//            is SettingEvent.NumberFormatClicked -> {
//                _extrasState.update { it.copy(showNumberFormat = true) }
//            }
//            is SettingEvent.NumberFormatSelected -> {
//                _extrasState.update { it.copy(selectedNumberFormat = event.numberformat, showNumberFormat = false) }
//            }
//            is SettingEvent.NumberFormatDismissed -> {
//                _extrasState.update { it.copy(showNumberFormat = false) }
//            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Backup logic: Room → Supabase
    // ──────────────────────────────────────────────────────────────────────────

    private fun startBackup() {
        viewModelScope.launch {
            _backupState.update { BackupViewState(isBackingUp = true) }

            try {
                val userId = settingPreferences.currentUserId.first()
                val username = settingPreferences.currentUsername.first() ?: "User"
                
                if (userId.isNullOrBlank()) {
                    _backupState.update { BackupViewState(resultMsg = "⚠️ Chưa đăng nhập. Không thể sao lưu.") }
                    return@launch
                }

                // 1. Thực hiện đồng bộ toàn bộ lên Supabase
                val success = syncRepository.syncAll(userId, username)

                if (success) {
                    // 2. Dọn dẹp dữ liệu local (Hard delete các bản ghi đã xóa tạm)
                    db.transactionDao().hardDeleteDeletedItems(userId)
                    db.walletDao().hardDeleteDeletedItems(userId)
                    db.categoryDao().hardDeleteDeletedItems(userId)
                    db.budgetDao().hardDeleteDeletedItems(userId)
                    db.savingDao().hardDeleteDeletedItems(userId)
                    db.savingRecordDao().hardDeleteDeletedItems(userId)

                    _backupState.update { BackupViewState(resultMsg = "✅ Sao lưu và dọn dẹp dữ liệu thành công.") }
                } else {
                    _backupState.update { BackupViewState(resultMsg = "⚠️ Sao lưu thất bại hoặc không hoàn tất.") }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Backup failed: ${e.message}", e)
                _backupState.update {
                    BackupViewState(resultMsg = "❌ Sao lưu thất bại: ${e.message ?: "Lỗi không xác định"}")
                }
            }
        }
    }

    companion object {
        fun factory(context: Context) = SettingViewModelFactory(context)
    }
}
