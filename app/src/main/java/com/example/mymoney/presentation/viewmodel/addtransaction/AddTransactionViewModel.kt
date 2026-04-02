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
import com.example.mymoney.data.repository.WalletRepositoryImpl
import com.example.mymoney.domain.model.ChatMessageModel
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.ChatRepository
import com.example.mymoney.domain.repository.WalletRepository
import com.example.mymoney.domain.usecase.AddTransactionUseCase
import com.example.mymoney.domain.usecase.EnsureDefaultWalletUseCase
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
 * Luồng đầy đủ khi user nhắn "bữa tối 20k":
 *  1. Hiển thị bubble user + bubble "..." ngay lập tức
 *  2. Lưu tin nhắn user vào Room (chat_messages)
 *  3. Gọi Groq API → parse ra { note, amount, type, category }
 *  4. Thay "..." bằng phản hồi AI
 *  5. Lưu tin nhắn AI vào Room
 *  6. Với mỗi giao dịch parse được:
 *     a. Lấy/tạo ví mặc định của user
 *     b. Lưu TransactionModel vào Room (kèm walletId)
 *     c. Cập nhật balance ví trong Room (income → cộng, expense → trừ)
 *     d. Lưu lên Supabase song song (không block UI)
 */
class AddTransactionViewModel(
    @Suppress("unused")
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val walletRepository: WalletRepository,
    private val ensureDefaultWallet: EnsureDefaultWalletUseCase,
    private val chatRepository: ChatRepository,
    private val supabaseTransactionRepo: SupabaseTransactionRepository,
    private val settingPreferences: SettingPreferences
) : ViewModel() {

    private val TAG = "AddTransactionVM"

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<AddTransactionNavEvent>()
    val navEvent: SharedFlow<AddTransactionNavEvent> = _navEvent.asSharedFlow()

    // sessionId là field của ViewModel — KHÔNG nằm trong UiState
    // → tạo 1 lần duy nhất, không mất khi recompose
    // → restore = dùng lại sessionId cũ nhất của user
    private var sessionId: String = java.util.UUID.randomUUID().toString()

    private var messageIdCounter = 0L
    private var isWaitingForAI = false
    private var submitDebounceJob: Job? = null

    init {
        viewModelScope.launch {
            // 1. Xóa tin nhắn cũ > 48h
            runCatching { chatRepository.deleteOldMessages() }

            val userId = settingPreferences.currentUserId.first() ?: return@launch

            // 2. Restore sessionId của phiên gần nhất (nếu có)
            val latestSession = runCatching {
                chatRepository.getLatestSessionId(userId)
            }.getOrNull()

            if (latestSession != null) {
                // Có lịch sử → dùng lại session cũ
                sessionId = latestSession
            }
            // Nếu null → giữ sessionId mới tạo (lần đầu dùng app)

            // 3. Load tất cả tin nhắn từ Room (subscribe Flow → tự cập nhật khi có tin mới)
            _uiState.update { it.copy(isLoading = true) }

            chatRepository.getAllMessagesByUser(userId).collect { stored ->
                val chatMessages = stored.map { model ->
                    ChatMessage(
                        id        = model.id,
                        content   = model.content,
                        sender    = if (model.sender == "user") ChatSender.USER else ChatSender.AI,
                        timestamp = model.timestamp
                    )
                }
                // Chỉ cập nhật messages từ Room nếu AI không đang gõ
                // (tránh overwrite bubble "•••" đang hiển thị)
                if (!isWaitingForAI) {
                    messageIdCounter = (chatMessages.maxOfOrNull { it.id } ?: 0L)
                    _uiState.update { state ->
                        state.copy(
                            messages  = chatMessages,
                            isEmpty   = chatMessages.isEmpty(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }

        // 4. Load tên ví mặc định cho chip header
        viewModelScope.launch {
            val userId = settingPreferences.currentUserId.first() ?: return@launch
            val wallet = runCatching { ensureDefaultWallet(userId) }.getOrNull()
            if (wallet != null) {
                _uiState.update { it.copy(walletName = wallet.name) }
            }
        }
    }

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.OnNoteChanged          -> _uiState.update { it.copy(noteInput = event.note) }
            is AddTransactionEvent.OnSubmitClicked        -> handleSubmit()
            is AddTransactionEvent.OnCameraClicked        -> { /* TODO */ }
            is AddTransactionEvent.OnMicClicked           -> { /* TODO */ }
            is AddTransactionEvent.OnParseSettingsClicked -> handleParseSettings()
            is AddTransactionEvent.OnTransferFundClicked  -> { /* TODO */ }
            is AddTransactionEvent.OnRecurringClicked     -> { /* TODO */ }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Core flow
    // ──────────────────────────────────────────────────────────────────────────

    private fun handleSubmit() {
        val noteText = _uiState.value.noteInput.trim()
        if (noteText.isBlank() || isWaitingForAI) return

        submitDebounceJob?.cancel()
        submitDebounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(200L)
            processUserMessage(noteText)
        }
    }

    private suspend fun processUserMessage(noteText: String) {
        isWaitingForAI = true
        val now = System.currentTimeMillis()

        // ── Step 1: Hiển thị bubble user + typing indicator ──
        val userMsgId    = ++messageIdCounter
        val typingId     = ++messageIdCounter
        val userBubble   = ChatMessage(id = userMsgId, content = noteText,  sender = ChatSender.USER)
        val typingBubble = ChatMessage(id = typingId,  content = "•••",     sender = ChatSender.AI)

        _uiState.update {
            it.copy(
                messages     = it.messages + userBubble + typingBubble,
                noteInput    = "",
                isEmpty      = false,
                errorMessage = null
            )
        }

        // ── Step 2: Lấy userId ──
        val userId = settingPreferences.currentUserId.first() ?: ""

        // Lưu tin nhắn user vào Room (fire-and-forget)
        viewModelScope.launch {
            runCatching {
                chatRepository.saveMessage(
                    ChatMessageModel(userId = userId, content = noteText,
                        sender = "user", sessionId = sessionId, timestamp = now)
                )
            }
        }

        // ── Step 3: Gọi Groq API ──
        try {
            val result = GroqService.chatWithParsing(noteText)

            // ── Step 4: Xây text AI và cập nhật bubble ──
            val aiText   = buildAIDisplayText(result)
            val aiBubble = ChatMessage(id = typingId, content = aiText, sender = ChatSender.AI)
            _uiState.update { state ->
                state.copy(messages = state.messages.map { if (it.id == typingId) aiBubble else it })
            }

            // Lưu tin nhắn AI vào Room (fire-and-forget)
            viewModelScope.launch {
                runCatching {
                    chatRepository.saveMessage(
                        ChatMessageModel(userId = userId, content = aiText,
                            sender = "ai", sessionId = sessionId,
                            timestamp = System.currentTimeMillis())
                    )
                }
            }

            // ── Step 5 & 6: Xử lý từng giao dịch parse được ──
            if (result.transactions.isNotEmpty() && userId.isNotBlank()) {
                viewModelScope.launch {
                    // Lấy ví mặc định một lần dùng cho tất cả giao dịch trong batch
                    val wallet = runCatching { ensureDefaultWallet(userId) }.getOrElse {
                        Log.e(TAG, "Cannot get/create wallet: ${it.message}")
                        return@launch
                    }

                    result.transactions.forEach { parsed ->
                        // ── Kiểm tra số dư trước khi lưu expense ──
                        val currentBalance = walletRepository.getTotalBalance(userId).first()

                        if (parsed.type == "expense" && currentBalance < parsed.amount) {
                            // Số dư không đủ → KHÔNG lưu, thêm bubble cảnh báo
                            val shortfall   = parsed.amount - currentBalance
                            val warnText    = buildInsufficientBalanceText(
                                txNote      = parsed.note,
                                txAmount    = parsed.amount,
                                balance     = currentBalance,
                                shortfall   = shortfall
                            )
                            val warnId  = ++messageIdCounter
                            val warnMsg = ChatMessage(id = warnId, content = warnText, sender = ChatSender.AI)
                            _uiState.update { s -> s.copy(messages = s.messages + warnMsg) }

                            // Lưu cảnh báo vào Room chat history
                            runCatching {
                                chatRepository.saveMessage(
                                    ChatMessageModel(
                                        userId    = userId,
                                        content   = warnText,
                                        sender    = "ai",
                                        sessionId = sessionId,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                            Log.d(TAG, "Insufficient balance: $currentBalance < ${parsed.amount}, skipped")
                            return@forEach  // bỏ qua giao dịch này
                        }

                        val transaction = TransactionModel(
                            note      = parsed.note,
                            amount    = parsed.amount,
                            type      = parsed.type,
                            category  = parsed.category,
                            walletId  = wallet.id,
                            timestamp = now
                        )

                        // 6a. Lưu giao dịch vào Room
                        val saveResult = runCatching { addTransactionUseCase(transaction) }
                        if (saveResult.isFailure) {
                            Log.e(TAG, "Room insert failed: ${saveResult.exceptionOrNull()?.message}")
                            return@forEach
                        }
                        Log.d(TAG, "Saved to Room: ${parsed.note} ${parsed.amount}")

                        // 6b. Cập nhật balance ví
                        //   income  → balance + amount
                        //   expense → balance - amount (đã kiểm tra đủ tiền ở trên)
                        val delta      = if (parsed.type == "income") parsed.amount else -parsed.amount
                        val newBalance = currentBalance + delta
                        runCatching {
                            walletRepository.updateWalletBalance(wallet.id, newBalance)
                        }.onFailure {
                            Log.e(TAG, "Wallet balance update failed: ${it.message}")
                        }
                        Log.d(TAG, "Wallet balance: $currentBalance → $newBalance (delta=$delta)")

                        // 6c. Upload lên Supabase (non-blocking, non-fatal)
                        launch {
                            runCatching {
                                supabaseTransactionRepo.insertTransaction(
                                    userId          = userId,
                                    note            = parsed.note,
                                    amount          = parsed.amount,
                                    type            = parsed.type,
                                    category        = parsed.category,
                                    timestampMillis = now
                                )
                            }.onFailure { Log.w(TAG, "Supabase insert failed (non-critical): ${it.message}") }
                        }
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
            viewModelScope.launch {
                runCatching {
                    chatRepository.saveMessage(
                        ChatMessageModel(userId = userId, content = errText,
                            sender = "ai", sessionId = sessionId,
                            timestamp = System.currentTimeMillis())
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
     * Tạo thông báo AI khi số dư ví không đủ để chi tiêu.
     * Không lưu giao dịch, chỉ hiện cảnh báo.
     */
    private fun buildInsufficientBalanceText(
        txNote: String,
        txAmount: Double,
        balance: Double,
        shortfall: Double
    ): String {
        val amountFmt   = formatAmount(txAmount)
        val balanceFmt  = formatAmount(balance)
        val shortFmt    = formatAmount(shortfall)
        return "⚠️ Số dư ví không đủ để ghi \"$txNote\"!\n\n" +
               "• Chi tiêu cần: ${amountFmt}đ\n" +
               "• Số dư hiện tại: ${balanceFmt}đ\n" +
               "• Còn thiếu: ${shortFmt}đ\n\n" +
               "💡 Hãy nộp thêm tiền vào ví trước nhé! " +
               "Bạn có thể nhắn \"nạp [số tiền]\" để thêm thu nhập."
    }

    private fun buildAIDisplayText(result: GroqService.ChatResult): String {
        if (result.transactions.isEmpty()) return result.displayText

        val txLines = result.transactions.joinToString("\n") { tx ->
            val sign   = if (tx.type == "income") "+" else "-"
            val amount = formatAmount(tx.amount)
            "• ${tx.note}: $sign${amount}đ  [${tx.category}]"
        }
        return "${result.displayText}\n\n✅ Đã lưu:\n$txLines"
    }

    private fun formatAmount(amount: Double): String =
        String.format(Locale.US, "%,.0f", amount).replace(',', '.')

    private fun handleParseSettings() {
        viewModelScope.launch { _navEvent.emit(AddTransactionNavEvent.NavigateToParseSettings) }
    }

    private fun mapAIError(e: Exception): String {
        val msg = e.message?.lowercase() ?: return "⚠️ Có lỗi xảy ra. Vui lòng thử lại."
        return when {
            "not configured" in msg || "groq_api_key" in msg ->
                "⚠️ API key chưa cấu hình. Thêm GROQ_API_KEY vào local.properties."
            "api key" in msg || "authentication" in msg || "api_key_invalid" in msg ->
                "⚠️ API key không hợp lệ. Kiểm tra tại https://console.groq.com/keys"
            "network" in msg || "connect" in msg || "timeout" in msg ->
                "⚠️ Lỗi kết nối mạng. Kiểm tra internet và thử lại."
            "rate limit" in msg || "quota exceeded" in msg ->
                "⚠️ Quá nhiều yêu cầu. Vui lòng đợi rồi thử lại."
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
                    val db         = AppDatabase.getInstance(context.applicationContext)
                    val txRepo     = TransactionRepositoryImpl(db.transactionDao())
                    val walletRepo = WalletRepositoryImpl(db.walletDao())
                    return AddTransactionViewModel(
                        getTransactionsUseCase  = GetTransactionsUseCase(txRepo),
                        addTransactionUseCase   = AddTransactionUseCase(txRepo),
                        walletRepository        = walletRepo,
                        ensureDefaultWallet     = EnsureDefaultWalletUseCase(walletRepo),
                        chatRepository          = ChatRepositoryImpl(db.chatMessageDao()),
                        supabaseTransactionRepo = SupabaseTransactionRepository(),
                        settingPreferences      = SettingPreferences(context.applicationContext)
                    ) as T
                }
            }
    }
}
