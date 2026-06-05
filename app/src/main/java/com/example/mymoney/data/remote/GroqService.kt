package com.example.mymoney.data.remote

import android.util.Log
import com.example.mymoney.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import java.io.File
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Service gọi Groq API (MIỄN PHÍ) - model llama3-8b-8192
 * Lấy key tại: https://console.groq.com/keys
 * Free tier: 30 req/phút, 14,400 req/ngày — thoải mái hơn Gemini nhiều
 */
object GroqService {
    private const val TAG = "GroqService"
    private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "llama-3.3-70b-versatile"

    private val SYSTEM_PROMPT = """
        Bạn là trợ lý tài chính AI trong ứng dụng MyMoney.
        Khi người dùng nhắn một giao dịch (ví dụ: "bữa tối 20k", "lương 10tr"), bạn phải:
        1. Phản hồi ngắn gọn, vui vẻ bằng tiếng Việt (1-2 câu, có thể dùng emoji)
        2. Trả về JSON ở cuối trong block ```json ... ```

        Format JSON bắt buộc:
        ```json
        {
          "transactions": [
            {
              "note": "Tên giao dịch",
              "amount": 20000,
              "type": "expense",
              "category": "Ăn uống"
            }
          ]
        }
        ```

        Quy tắc chuyển đổi số tiền:
        - "k" hoặc "K" = × 1,000  (20k → 20000)
        - "tr" hoặc "triệu" = × 1,000,000  (10tr → 10000000)
        - Số thuần túy = nguyên xi (500000 → 500000)

        Quy tắc xác định type:
        - Mặc định: "expense" (chi tiêu)
        - "income" khi rõ ràng là thu nhập: lương, thưởng, nhận tiền, bán hàng...

        Danh mục (category) gợi ý:
        - Chi tiêu: Ăn uống, Di chuyển, Mua sắm, Giải trí, Sức khỏe, Giáo dục, Hóa đơn, Khác
        - Thu nhập: Thu nhập, Thưởng, Đầu tư, Khác

        Nếu tin nhắn không liên quan đến giao dịch tài chính,
        hãy trả lời thân thiện và KHÔNG trả về JSON.
    """.trimIndent()

    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    // ── Request ──
    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        @SerialName("max_tokens") val maxTokens: Int = 1024,
        val temperature: Double = 0.7
    )

    @Serializable
    private data class Message(val role: String, val content: String)

    // ── Response (success) ──
    @Serializable
    private data class ChatResponse(val choices: List<Choice>)

    @Serializable
    private data class Choice(val message: Message)

    // ── Response (error) ──
    @Serializable
    private data class ErrorWrapper(val error: ErrorDetail? = null)

    @Serializable
    private data class ErrorDetail(
        val message: String = "Unknown error",
        val type: String = "",
        val code: String = ""
    )

    /**
     * Gửi tin nhắn tới Groq (llama3-8b) và nhận phản hồi.
     * Giao diện giữ nguyên tên "GroqService.chat()" để không cần sửa ViewModel.
     */
    suspend fun chat(userMessage: String): String {
        return chatWithParsing(userMessage).displayText
    }

    /**
     * Kết quả parse từ Groq: text hiển thị + danh sách giao dịch đã parse.
     */
    data class ChatResult(
        val displayText: String,
        val transactions: List<ParsedTransaction> = emptyList()
    )

    /**
     * Giao dịch đã được AI parse ra từ tin nhắn.
     */
    data class ParsedTransaction(
        val note: String,
        val amount: Double,
        val type: String,       // "income" | "expense"
        val category: String
    )

    /**
     * Gửi tin nhắn, parse JSON giao dịch từ response nếu có.
     * Trả về [ChatResult] gồm text hiển thị + list giao dịch đã parse.
     */
    suspend fun chatWithParsing(userMessage: String): ChatResult {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank()) {
            Log.e(TAG, "⚠️ GROQ_API_KEY trống!")
            throw IllegalStateException("GROQ_API_KEY chưa cấu hình. Thêm vào local.properties")
        }

        val httpResponse = try {
            client.post(GROQ_URL) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(
                    ChatRequest(
                        model = MODEL,
                        messages = listOf(
                            Message(role = "system", content = SYSTEM_PROMPT),
                            Message(role = "user", content = userMessage)
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error: ${e.message}", e)
            throw e
        }

        val rawBody = httpResponse.bodyAsText()
        Log.d(TAG, "HTTP ${httpResponse.status.value}: $rawBody")

        if (!httpResponse.status.isSuccess()) {
            val errorWrapper = runCatching { json.decodeFromString<ErrorWrapper>(rawBody) }.getOrNull()
            val errorMsg = errorWrapper?.error?.message ?: "HTTP ${httpResponse.status.value}"
            throw Exception(errorMsg)
        }

        val response = json.decodeFromString<ChatResponse>(rawBody)
        val fullText = response.choices.firstOrNull()?.message?.content
            ?: "Xin lỗi, mình chưa hiểu ý bạn. Thử lại nhé! 😊"

        // Tách phần text hiển thị (bỏ JSON block)
        val displayText = fullText
            .replace(Regex("```json[\\s\\S]*?```"), "")
            .trim()
            .ifBlank { "✅ Đã ghi nhận giao dịch!" }

        // Tìm và parse JSON block nếu có
        val jsonMatch = Regex("```json([\\s\\S]*?)```").find(fullText)
        val transactions = if (jsonMatch != null) {
            try {
                val jsonBlock = jsonMatch.groupValues[1].trim()
                val parsed = json.decodeFromString<TransactionListWrapper>(jsonBlock)
                parsed.transactions.map { dto ->
                    ParsedTransaction(
                        note     = dto.note,
                        amount   = dto.amount,
                        type     = dto.type,
                        category = dto.category
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse transaction JSON: ${e.message}")
                emptyList()
            }
        } else emptyList()

        Log.d(TAG, "Parsed ${transactions.size} transaction(s)")
        return ChatResult(displayText = displayText, transactions = transactions)
    }

    /**
     * Nhận dạng giọng nói từ file audio bằng Groq Whisper API.
     * @param audioFile File âm thanh (m4a/mp4) đã ghi từ VoiceRecorder
     * @return Văn bản đã nhận dạng
     */
    suspend fun transcribeAudio(audioFile: File): String {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank()) throw IllegalStateException("GROQ_API_KEY chưa cấu hình")

        Log.d(TAG, "Uploading audio file: ${audioFile.absolutePath} (${audioFile.length()} bytes)")

        // Manually create multipart payload
        val boundary = UUID.randomUUID().toString()
        val body = buildMultipartBody(boundary, audioFile)

        val response = client.post("https://api.groq.com/openai/v1/audio/transcriptions") {
            header("Authorization", "Bearer $apiKey")
            header("Content-Type", "multipart/form-data; boundary=$boundary")
            setBody(body)
        }

        if (!response.status.isSuccess()) {
            val errorText = response.bodyAsText()
            Log.e(TAG, "Whisper error ${response.status.value}: $errorText")
            throw Exception("Lỗi nhận dạng giọng nói: ${response.status.value}")
        }
        val result = response.bodyAsText().trim()
        Log.d(TAG, "Whisper transcript: $result")
        return result
    }

    private fun buildMultipartBody(boundary: String, audioFile: File): ByteArray {
        val sb = StringBuilder()
        val CRLF = "\r\n"

        // model field
        sb.append("--$boundary$CRLF")
        sb.append("Content-Disposition: form-data; name=\"model\"$CRLF")
        sb.append("$CRLF")
        sb.append("whisper-large-v3$CRLF")

        // language field
        sb.append("--$boundary$CRLF")
        sb.append("Content-Disposition: form-data; name=\"language\"$CRLF")
        sb.append("$CRLF")
        sb.append("vi$CRLF")

        // response_format field
        sb.append("--$boundary$CRLF")
        sb.append("Content-Disposition: form-data; name=\"response_format\"$CRLF")
        sb.append("$CRLF")
        sb.append("text$CRLF")

        // file field
        sb.append("--$boundary$CRLF")
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"audio.m4a\"$CRLF")
        sb.append("Content-Type: audio/m4a$CRLF")
        sb.append("$CRLF")

        val headerBytes = sb.toString().toByteArray(Charsets.UTF_8)
        val fileBytes = audioFile.readBytes()
        val footerBytes = "$CRLF--$boundary--$CRLF".toByteArray(Charsets.UTF_8)

        val result = ByteArray(headerBytes.size + fileBytes.size + footerBytes.size)
        var pos = 0

        System.arraycopy(headerBytes, 0, result, pos, headerBytes.size)
        pos += headerBytes.size

        System.arraycopy(fileBytes, 0, result, pos, fileBytes.size)
        pos += fileBytes.size

        System.arraycopy(footerBytes, 0, result, pos, footerBytes.size)

        return result
    }

    @Serializable
    private data class TransactionListWrapper(
        val transactions: List<ParsedTransactionDto> = emptyList()
    )

    @Serializable
    private data class ParsedTransactionDto(
        val note: String = "",
        val amount: Double = 0.0,
        val type: String = "expense",
        val category: String = "Khác"
    )
}
