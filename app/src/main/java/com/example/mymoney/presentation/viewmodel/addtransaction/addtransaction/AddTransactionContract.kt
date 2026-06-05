package com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event, NavEvent cho AIChatScreen (trước đây AddTransactionScreen)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Người gửi tin nhắn trong cuộc trò chuyện.
 */
enum class ChatSender {
    USER,   // Tin nhắn của người dùng (hiển thị bên phải)
    AI      // Tin nhắn của AI (hiển thị bên trái)
}

/**
 * Trạng thái ghi âm giọng nói.
 * IDLE       → chưa làm gì
 * RECORDING  → đang ghi (user nhấn giữ mic)
 * PROCESSING → đang gửi audio lên Groq Whisper để nhận dạng
 * RECORDED   → đã nhận dạng xong, văn bản xuất hiện trong noteInput
 */
enum class VoiceRecordingState { IDLE, RECORDING, PROCESSING, RECORDED }

/**
 * Một tin nhắn trong cuộc trò chuyện giữa người dùng và AI.
 *
 * @param id        Mã định danh duy nhất
 * @param content   Nội dung tin nhắn
 * @param sender    Người gửi: USER hoặc AI
 * @param timestamp Thời điểm gửi (epoch millis)
 */
data class ChatMessage(
    val id: Long = 0L,
    val content: String,
    val sender: ChatSender,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Trạng thái giao diện màn hình Chat AI.
 * Immutable data class — cập nhật bằng copy().
 *
 * @param messages        Danh sách tin nhắn trong cuộc trò chuyện
 * @param noteInput       Nội dung người dùng đang nhập trong ô chat
 * @param isLoading       Đang tải dữ liệu ban đầu hay không
 * @param isEmpty         true khi chưa có tin nhắn nào
 * @param walletName      Tên ví hiện tại (hiển thị trên top bar)
 * @param errorMessage    Thông báo lỗi (null = không lỗi)
 * @param voiceState      Trạng thái ghi âm hiện tại
 * @param isVoicePlaying  Đang phát lại âm thanh hay không
 */
data class AddTransactionUiState(
    val messages: List<ChatMessage> = emptyList(),
    val noteInput: String = "",
    val isLoading: Boolean = true,        // true từ đầu → không flash màn trống khi restore từ Room
    val isEmpty: Boolean = true,
    val walletName: String = "Ví chính",
    val errorMessage: String? = null,
    val voiceState: VoiceRecordingState = VoiceRecordingState.IDLE,
    val isVoicePlaying: Boolean = false
)

/**
 * Sự kiện người dùng gửi từ UI lên ViewModel.
 * Sealed class — mỗi hành động là 1 subclass.
 */
sealed class AddTransactionEvent {
    /** Người dùng thay đổi nội dung ô nhập ghi chú */
    data class OnNoteChanged(val note: String) : AddTransactionEvent()

    /** Người dùng nhấn nút gửi (nút mũi tên xanh) */
    data object OnSubmitClicked : AddTransactionEvent()

    /** Người dùng nhấn nút camera */
    data object OnCameraClicked : AddTransactionEvent()

    /** (Legacy) Người dùng nhấn nút microphone một lần */
    data object OnMicClicked : AddTransactionEvent()

    /** Người dùng BẮT ĐẦU nhấn giữ mic → bắt đầu ghi âm */
    data object OnMicPressStart : AddTransactionEvent()

    /** Người dùng THẢ mic → dừng ghi, gửi Whisper để nhận dạng */
    data object OnMicPressEnd : AddTransactionEvent()

    /** Người dùng nhấn nút phát lại / dừng phát */
    data object OnVoicePlayback : AddTransactionEvent()

    /** Người dùng hủy bỏ ghi âm và xóa văn bản đã nhận dạng */
    data object OnVoiceCancel : AddTransactionEvent()

    /** Người dùng nhấn nút settings (cài đặt parsing) */
    data object OnParseSettingsClicked : AddTransactionEvent()

    /** Người dùng nhấn "Di chuyển quỹ" */
    data object OnTransferFundClicked : AddTransactionEvent()

    /** Người dùng nhấn "Giao dịch định kỳ" */
    data object OnRecurringClicked : AddTransactionEvent()
}

/**
 * Side-effect điều hướng — ViewModel phát qua SharedFlow.
 * UI collect 1 lần trong LaunchedEffect(Unit).
 */
sealed class AddTransactionNavEvent {
    /** Quay lại màn hình trước */
    data object NavigateBack : AddTransactionNavEvent()

    /** Mở màn hình cài đặt parsing giao dịch */
    data object NavigateToParseSettings : AddTransactionNavEvent()

    /** Mở màn hình giao dịch định kỳ */
    data class NavigateToRecurring(val walletId: Long) : AddTransactionNavEvent()
}
