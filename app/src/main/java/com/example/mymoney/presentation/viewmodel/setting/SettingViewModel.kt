package com.example.mymoney.presentation.viewmodel.setting

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.AuthRepositoryImpl
import com.example.mymoney.data.repository.SupabaseTransactionRepository
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.domain.repository.AuthRepository
import com.example.mymoney.domain.repository.TransactionRepository
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

/**
 * ViewModel quản lý logic và trạng thái cho màn hình Cài đặt.
 */
class SettingViewModel(
    private val settingPreferences: SettingPreferences,
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val supabaseTransactionRepo: SupabaseTransactionRepository
) : ViewModel() {

    private val TAG = "SettingViewModel"

    // ── UI state: merge DataStore flows + backup state ──
    private val _backupState = MutableStateFlow(BackupViewState())

    val uiState: StateFlow<SettingUiState> =
        combine(
            settingPreferences.isThousandSeparatorEnabled,
            settingPreferences.currentUsername,
            _backupState
        ) { enabled, username, backup ->
            SettingUiState(
                isThousandSeparatorEnabled = enabled,
                username                   = username,
                isBackingUp                = backup.isBackingUp,
                showBackupConfirmDialog    = backup.showDialog,
                backupResultMessage        = backup.resultMsg
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
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Backup logic: Room → Supabase
    // ──────────────────────────────────────────────────────────────────────────

    private fun startBackup() {
        viewModelScope.launch {
            // 1. Bắt đầu loading
            _backupState.update { BackupViewState(isBackingUp = true) }

            try {
                // 2. Lấy userId từ DataStore
                val userId = settingPreferences.currentUserId.first()
                if (userId.isNullOrBlank()) {
                    _backupState.update { BackupViewState(resultMsg = "⚠️ Chưa đăng nhập. Không thể sao lưu.") }
                    return@launch
                }

                // 3. Đọc tất cả giao dịch từ Room (lấy 1 snapshot, không subscribe)
                val transactions = transactionRepository.getAllTransactions().first()

                if (transactions.isEmpty()) {
                    _backupState.update { BackupViewState(resultMsg = "ℹ️ Không có giao dịch nào để sao lưu.") }
                    return@launch
                }

                // 4. Map sang DTO
                val dtos = transactions.map { tx ->
                    SupabaseTransactionRepository.TransactionItem(
                        note            = tx.note,
                        amount          = tx.amount,
                        type            = tx.type,
                        category        = tx.category,
                        timestampMillis = tx.timestamp
                    )
                }

                // 5. Upsert lên Supabase
                val count = supabaseTransactionRepo.upsertAll(userId, dtos)

                val msg = if (count == dtos.size)
                    "✅ Đã sao lưu $count giao dịch lên đám mây."
                else
                    "⚠️ Sao lưu một phần: $count/${dtos.size} giao dịch thành công."

                _backupState.update { BackupViewState(resultMsg = msg) }
                Log.d(TAG, msg)

            } catch (e: Exception) {
                Log.e(TAG, "Backup failed: ${e.message}", e)
                _backupState.update {
                    BackupViewState(resultMsg = "❌ Sao lưu thất bại: ${e.message ?: "Lỗi không xác định"}")
                }
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getInstance(context.applicationContext)
                    return SettingViewModel(
                        settingPreferences      = SettingPreferences(context.applicationContext),
                        authRepository          = AuthRepositoryImpl(),
                        transactionRepository   = TransactionRepositoryImpl(db.transactionDao()),
                        supabaseTransactionRepo = SupabaseTransactionRepository(db.categoryDao())
                    ) as T
                }
            }
    }
}
