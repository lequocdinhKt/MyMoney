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
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.ChatMessage
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.ChatSender
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel: quản lý logic và trạng thái cho màn hình Chat AI
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel cho AIChatScreen (trước đây AddTransactionScreen).
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

    /** Counter để tạo id duy nhất cho mỗi tin nhắn */
    private var messageIdCounter = 0L

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
     * Cập nhật nội dung ô nhập.
     */
    private fun handleNoteChanged(note: String) {
        _uiState.update { it.copy(noteInput = note) }
    }

    /**
     * Xử lý nhấn nút gửi:
     * 1. Thêm tin nhắn người dùng vào danh sách chat
     * 2. TODO: Gửi tin nhắn tới AI → nhận phản hồi → thêm tin nhắn AI
     * 3. TODO: AI parse giao dịch và tự động lưu vào Room
     */
    private fun handleSubmit() {
        val noteText = _uiState.value.noteInput.trim()
        if (noteText.isBlank()) return

        // Thêm tin nhắn người dùng vào chat
        val userMessage = ChatMessage(
            id = ++messageIdCounter,
            content = noteText,
            sender = ChatSender.USER
        )

        _uiState.update {
            val updatedMessages = it.messages + userMessage
            it.copy(
                messages = updatedMessages,
                noteInput = "",
                isEmpty = false,
                errorMessage = null
            )
        }

        // TODO: Gửi noteText tới AI service, nhận phản hồi, thêm ChatMessage(sender=AI)
        //       Sau đó AI parse giao dịch và gọi addTransactionUseCase() để lưu vào Room
        //       Xem file AI_INTEGRATION_GUIDE.md để biết cách triển khai
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
