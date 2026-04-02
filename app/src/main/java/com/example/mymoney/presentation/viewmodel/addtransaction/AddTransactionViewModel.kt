package com.example.mymoney.presentation.viewmodel.addtransaction

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.remote.GroqService
import com.example.mymoney.data.repository.ChatRepositoryImpl
import com.example.mymoney.data.repository.SupabaseTransactionRepository
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.domain.model.ChatMessageModel
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.ChatRepository
import com.example.mymoney.domain.usecase.AddTransactionUseCase
import com.example.mymoney.domain.usecase.GetTransactionsUseCase
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionNavEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionUiState
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.ChatMessage
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.ChatSender
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel cho AIChatScreen.
 *
 * Luồng khi user gửi tin nhắn:
 *  1. Hiển thị bubble user + bubble "..." (typing) ngay lập tức
 *  2. Lưu tin nhắn user vào Room (chat_messages)
 *  3. Gọi Groq API → nhận ChatResult (text + transactions)
 *  4. Thay bubble "..." bằng text AI
 *  5. Lưu tin nhắn AI vào Room
 *  6. Với mỗi giao dịch parse được: lưu Room + Supabase song song
 *  7. Xóa tin nhắn Room > 48h (housekeeping)
 */
class AddTransactionViewModel(
    @Suppress("unused")
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val chatRepository: ChatRepository,
    private val supabaseTransactionRepo: SupabaseTransactionRepository,
    private val settingPreferences: SettingPreferences
) : ViewModel() {

    private val TAG = "AddTransactionVM"

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<AddTransactionNavEvent>()
    val navEvent: SharedFlow<AddTransactionNavEvent> = _navEvent.asSharedFlow()

    private var messageIdCounter = 0L
    private var isWaitingForAI = false
    private var submitDebounceJob: Job? = null

    init {
        // Xóa tin nhắn cũ > 48h khi mở màn hình
        viewModelScope.launch {
            runCatching { chatRepository.deleteOldMessages() }
        }
    }

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.OnNoteChanged         -> _uiState.update { it.copy(noteInput = event.note) }
            is AddTransactionEvent.OnSubmitClicked       -> handleSubmit()
            is AddTransactionEvent.OnCameraClicked       -> { /* TODO */ }
            is AddTransactionEvent.OnMicClicked          -> { /* TODO */ }
            is AddTransactionEvent.OnParseSettingsClicked-> handleParseSettings()
            is AddTransactionEvent.OnTransferFundClicked -> { /* TODO */ }
            is AddTransactionEvent.OnRecurringClicked    -> { /* TODO */ }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Core: gửi tin nhắn, gọi AI, lưu Room + Supabase
    // ──────────────────────────────────────────────────────────────────────────

    private fun handleSubmit() {
        val noteText = _uiState.value.noteInput.trim()
        if (noteText.isBlank() || isWaitingForAI) return

        submitDebounceJob?.cancel()
        submitDebounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300L)
            processUserMessage(noteText)
        }
    }

    private suspend fun processUserMessage(noteText: String) {
        isWaitingForAI = true
        val sessionId = _uiState.value.sessionId
        val now = System.currentTimeMillis()

        // ── 1. Hiển thị bubble user + typing ──
        val userMsgId = ++messageIdCounter
        val typingId  = ++messageIdCounter
        val userBubble   = ChatMessage(id = userMsgId,  content = noteText,      sender = ChatSender.USER)
        val typingBubble = ChatMessage(id = typingId,   content = "...",         sender = ChatSender.AI)

        _uiState.update {
            it.copy(
                messages    = it.messages + userBubble + typingBubble,
                noteInput   = "",
                isEmpty     = false,
                errorMessage= null
            )
        }

        // ── 2. Lưu tin nhắn user vào Room (bất đồng bộ, không block UI) ──
        val userId = settingPreferences.currentUserId.first() ?: ""
        viewModelScope.launch {
            runCatching {
                chatRepository.saveMessage(
                    ChatMessageModel(
                        userId     = userId,
                        content    = noteText,
                        sender     = "user",
                        sessionId  = sessionId,
                        timestamp  = now
                    )
                )
            }
        }

        // ── 3. Gọi Groq API ──
        try {
            val result = GroqService.chatWithParsing(noteText)

            // ── 4. Cập nhật bubble typing → text AI ──
            val aiText = buildAIDisplayText(result)
            val aiBubble = ChatMessage(id = typingId, content = aiText, sender = ChatSender.AI)
            _uiState.update { state ->
                state.copy(messages = state.messages.map { if (it.id == typingId) aiBubble else it })
            }

            // ── 5. Lưu tin nhắn AI vào Room ──
            viewModelScope.launch {
                runCatching {
                    chatRepository.saveMessage(
                        ChatMessageModel(
                            userId    = userId,
                            content   = aiText,
                            sender    = "ai",
                            sessionId = sessionId,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }

            // ── 6. Lưu giao dịch: Room (local) + Supabase (remote) ──
            result.transactions.forEach { parsed ->
                val transaction = TransactionModel(
                    note      = parsed.note,
                    amount    = parsed.amount,
                    type      = parsed.type,
                    category  = parsed.category,
                    timestamp = now
                )
                viewModelScope.launch {
                    // Room — local cache
                    runCatching { addTransactionUseCase(transaction) }
                        .onFailure { Log.e(TAG, "Room insert failed: ${it.message}") }

                    // Supabase — remote (chỉ khi có userId)
                    if (userId.isNotBlank()) {
                        val supabaseId = supabaseTransactionRepo.insertTransaction(transaction, userId)
                        Log.d(TAG, "Supabase transaction id: $supabaseId")
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "AI error: ${e.message}", e)
            val errText = mapAIError(e)
            _uiState.update { state ->
                state.copy(
                    messages = state.messages.map {
                        if (it.id == typingId) it.copy(content = errText) else it
                    }
                )
            }
            // Lưu lỗi vào Room để giữ lịch sử
            viewModelScope.launch {
                runCatching {
                    chatRepository.saveMessage(
                        ChatMessageModel(
                            userId    = userId,
                            content   = errText,
                            sender    = "ai",
                            sessionId = sessionId,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        } finally {
            isWaitingForAI = false
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Xây text hiển thị cho bubble AI: kết hợp displayText + xác nhận giao dịch đã lưu.
     */
    private fun buildAIDisplayText(result: GroqService.ChatResult): String {
        if (result.transactions.isEmpty()) return result.displayText

        val txLines = result.transactions.joinToString("\n") { tx ->
            val sign   = if (tx.type == "income") "+" else "-"
            val amount = formatAmount(tx.amount)
            "• ${tx.note}: $sign$amount đ  [${tx.category}]"
        }
        return "${result.displayText}\n\n✅ Đã lưu giao dịch:\n$txLines"
    }

    private fun formatAmount(amount: Double): String {
        return String.format(Locale.US, "%,.0f", amount).replace(',', '.')
    }

    private fun handleParseSettings() {
        viewModelScope.launch { _navEvent.emit(AddTransactionNavEvent.NavigateToParseSettings) }
    }

    private fun mapAIError(e: Exception): String {
        val msg = e.message?.lowercase() ?: return "⚠️ Có lỗi xảy ra. Vui lòng thử lại."
        return when {
            "not configured" in msg || "groq_api_key" in msg ->
                "⚠️ API key chưa cấu hình. Thêm GROQ_API_KEY=your-key vào local.properties."
            "api key" in msg || "authentication" in msg || "api_key_invalid" in msg ->
                "⚠️ API key không hợp lệ. Kiểm tra tại https://console.groq.com/keys"
            "network" in msg || "connect" in msg || "timeout" in msg ->
                "⚠️ Lỗi kết nối mạng. Kiểm tra internet và thử lại."
            "rate limit" in msg || "quota exceeded" in msg ->
                "⚠️ Quá nhiều yêu cầu. Vui lòng đợi một lúc rồi thử lại."
            else -> "⚠️ ${e.message ?: "Lỗi không xác định"}. Vui lòng thử lại."
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Factory
    // ──────────────────────────────────────────────────────────────────────────

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db   = AppDatabase.getInstance(context.applicationContext)
                    val repo = TransactionRepositoryImpl(db.transactionDao())
                    return AddTransactionViewModel(
                        getTransactionsUseCase   = GetTransactionsUseCase(repo),
                        addTransactionUseCase    = AddTransactionUseCase(repo),
                        chatRepository           = ChatRepositoryImpl(db.chatMessageDao()),
                        supabaseTransactionRepo  = SupabaseTransactionRepository(),
                        settingPreferences       = SettingPreferences(context.applicationContext)
                    ) as T
                }
            }
    }
}
