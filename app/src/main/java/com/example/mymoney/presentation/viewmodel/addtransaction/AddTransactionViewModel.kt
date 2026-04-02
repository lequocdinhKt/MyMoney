package com.example.mymoney.presentation.viewmodel.addtransaction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.remote.GroqService
import com.example.mymoney.data.repository.TransactionRepositoryImpl
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
import kotlinx.coroutines.Job

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
    @Suppress("unused") // Sẽ dùng khi implement lịch sử giao dịch
    private val getTransactionsUseCase: GetTransactionsUseCase,
    @Suppress("unused") // Sẽ dùng khi có DB schema để lưu giao dịch từ AI
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

    /** Guard: chặn gọi API khi đang chờ phản hồi từ Gemini */
    private var isWaitingForAI = false

    /**
     * Debounce Job: huỷ request cũ nếu user nhấn Submit liên tục.
     * Chỉ thực sự gọi API sau 500ms kể từ lần nhấn cuối cùng.
     */
    private var submitDebounceJob: Job? = null

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
     * Xử lý gửi tin nhắn:
     * 1. Debounce 500ms — huỷ nếu user nhấn liên tục
     * 2. Hiển thị tin nhắn user ngay lập tức
     * 3. Hiển thị "đang nhập..." trong khi chờ AI
     * 4. Gọi Gemini API → nhận phản hồi
     * 5. Thay thế bubble "đang nhập..." bằng phản hồi thật
     */
    private fun handleSubmit() {
        val noteText = _uiState.value.noteInput.trim()
        if (noteText.isBlank()) return

        // ✅ Guard: đang chờ AI phản hồi → không gọi tiếp
        if (isWaitingForAI) return

        // ✅ Debounce 500ms: huỷ job cũ nếu user nhấn liên tục
        submitDebounceJob?.cancel()
        submitDebounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500L)

            isWaitingForAI = true

            // Bước 1: Thêm tin nhắn user
            val userMessage = ChatMessage(
                id = ++messageIdCounter,
                content = noteText,
                sender = ChatSender.USER
            )
            // Bước 2: Thêm bubble "đang nhập..."
            val typingId = ++messageIdCounter
            val typingMessage = ChatMessage(
                id = typingId,
                content = "...",
                sender = ChatSender.AI
            )
            _uiState.update {
                it.copy(
                    messages = it.messages + userMessage + typingMessage,
                    noteInput = "",
                    isEmpty = false,
                    errorMessage = null
                )
            }

            // Gọi Gemini API
            try {
                val aiResponse = GroqService.chat(noteText)
                val aiMessage = ChatMessage(
                    id = typingId,
                    content = aiResponse.trim(),
                    sender = ChatSender.AI
                )
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map {
                            if (it.id == typingId) aiMessage else it
                        }
                    )
                }
            } catch (e: Exception) {
                val errorMsg = mapAIError(e)
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map {
                            if (it.id == typingId) it.copy(content = errorMsg) else it
                        }
                    )
                }
            } finally {
                // ✅ Luôn mở lại guard dù thành công hay lỗi
                isWaitingForAI = false
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

    /**
     * Map Gemini API exception thành thông báo lỗi thân thiện.
     * Xử lý các lỗi phổ biến: API key, network, rate limit, v.v.
     */
    private fun mapAIError(e: Exception): String {
        val message = e.message?.lowercase() ?: return "⚠️ Có lỗi xảy ra. Vui lòng thử lại."

        return when {
            // ── Lỗi API Key ──
            "not configured" in message || "gemini_api_key" in message ->
                "⚠️ API key chưa cấu hình. Thêm GEMINI_API_KEY=your-key vào local.properties."

            "api key" in message || "apikey" in message || "authentication" in message ||
            "invalid api key" in message || "api_key_invalid" in message ->
                "⚠️ API key không hợp lệ hoặc đã hết quota. Kiểm tra lại API key tại https://aistudio.google.com/app/apikey"

            // ── Lỗi Network ──
            "network" in message || "connect" in message || "timeout" in message ||
            "unable to resolve" in message || "socket" in message ->
                "⚠️ Lỗi kết nối mạng. Kiểm tra internet và thử lại."

            // ── Lỗi Rate Limit ──
            "rate limit" in message || "quota exceeded" in message || "too many requests" in message ->
                "⚠️ Quá nhiều yêu cầu. API cho phép 15 request/phút. Vui lòng đợi một lúc rồi thử lại."

            // ── Lỗi Permission ──
            "permission denied" in message || "forbidden" in message ->
                "⚠️ Bạn không có quyền truy cập. Kiểm tra lại API key."

            // ── Lỗi Server ──
            "unexpected" in message || "something went wrong" in message || "something unexpected" in message ->
                "⚠️ Máy chủ AI gặp lỗi. Vui lòng thử lại sau."

            "empty" in message && "response" in message ->
                "⚠️ Máy chủ AI không phản hồi. Thử lại nhé!"

            else -> "⚠️ Lỗi: ${e.message ?: "Không xác định"}. Vui lòng thử lại."
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
