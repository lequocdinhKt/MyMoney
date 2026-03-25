package com.example.mymoney.presentation.viewmodel.addtransaction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.usecase.AddTransactionUseCase
import com.example.mymoney.domain.usecase.GetTransactionsUseCase
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionNavEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel: quản lý logic và trạng thái cho màn hình Thêm Giao Dịch
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel cho AddTransactionScreen.
 *
 * Luồng UDF:
 *   UI gửi [AddTransactionEvent] → ViewModel cập nhật [AddTransactionUiState]
 *   hoặc phát [AddTransactionNavEvent] qua SharedFlow → UI collect và navigate
 *
 * @param getTransactionsUseCase  Use case lấy danh sách giao dịch
 * @param addTransactionUseCase   Use case thêm giao dịch mới
 */
class AddTransactionViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    /** State cho UI quan sát – chỉ đọc */
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    // replay = 0: không phát lại event cũ khi subscriber mới collect
    private val _navEvent = MutableSharedFlow<AddTransactionNavEvent>()
    /** Navigation side-effect – UI collect 1 lần qua LaunchedEffect(Unit) */
    val navEvent: SharedFlow<AddTransactionNavEvent> = _navEvent.asSharedFlow()

    init {
        // Bắt đầu observe danh sách giao dịch từ Room
        observeTransactions()
    }

    // ── Observe danh sách giao dịch từ Room ──

    /**
     * Lắng nghe Flow từ Room → cập nhật state tự động.
     * Khi dữ liệu thay đổi (thêm/xoá), UI sẽ recompose.
     */
    private fun observeTransactions() {
        viewModelScope.launch {
            getTransactionsUseCase().collect { transactions ->
                _uiState.update {
                    it.copy(
                        transactions = transactions,
                        isEmpty = transactions.isEmpty(),
                        isLoading = false
                    )
                }
            }
        }
    }

    // ── Xử lý event từ UI ──

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.OnNoteChanged -> handleNoteChanged(event.note)
            is AddTransactionEvent.OnSubmitClicked -> handleSubmit()
            is AddTransactionEvent.OnCameraClicked -> { /* TODO: Mở camera chụp hoá đơn */ }
            is AddTransactionEvent.OnMicClicked -> { /* TODO: Mở nhận dạng giọng nói */ }
            is AddTransactionEvent.OnParseSettingsClicked -> handleParseSettings()
            is AddTransactionEvent.OnTransferFundClicked -> { /* TODO: Mở di chuyển quỹ */ }
            is AddTransactionEvent.OnRecurringClicked -> { /* TODO: Mở giao dịch định kỳ */ }
        }
    }

    /**
     * Cập nhật nội dung ô nhập ghi chú.
     */
    private fun handleNoteChanged(note: String) {
        _uiState.update { it.copy(noteInput = note) }
    }

    /**
     * Xử lý nhấn nút gửi:
     * - Parse ghi chú thành giao dịch (tạm thời: lưu toàn bộ note làm 1 giao dịch)
     * - Gọi addTransactionUseCase
     * - Xoá nội dung ô nhập sau khi thành công
     */
    private fun handleSubmit() {
        val noteText = _uiState.value.noteInput.trim()
        if (noteText.isBlank()) return

        viewModelScope.launch {
            try {
                // TODO: Parse "Bữa tối 100k, mua sắm 400k" thành nhiều giao dịch riêng biệt
                // Hiện tại tạm lưu toàn bộ note làm 1 giao dịch với amount = 0
                val transaction = TransactionModel(
                    note = noteText,
                    amount = 0.0,       // TODO: Parse số tiền từ ghi chú
                    type = "expense",
                    category = "Khác"
                )
                addTransactionUseCase(transaction)

                // Xoá ô nhập sau khi thêm thành công
                _uiState.update { it.copy(noteInput = "", errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Có lỗi xảy ra")
                }
            }
        }
    }

    /**
     * Mở cài đặt parsing giao dịch.
     */
    private fun handleParseSettings() {
        viewModelScope.launch {
            _navEvent.emit(AddTransactionNavEvent.NavigateToParseSettings)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory: tạo ViewModel với dependencies
    // Dùng thủ công khi chưa có DI framework (Hilt/Koin)
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        /**
         * Tạo [ViewModelProvider.Factory] nhận [Context].
         *
         * Cách dùng trong Composable:
         * ```kotlin
         * val ctx = LocalContext.current
         * val vm: AddTransactionViewModel = viewModel(
         *     factory = AddTransactionViewModel.factory(ctx)
         * )
         * ```
         */
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getInstance(context.applicationContext)
                    val dao = db.transactionDao()
                    val repository = TransactionRepositoryImpl(dao)
                    return AddTransactionViewModel(
                        getTransactionsUseCase = GetTransactionsUseCase(repository),
                        addTransactionUseCase = AddTransactionUseCase(repository)
                    ) as T
                }
            }
    }
}
