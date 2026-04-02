# 🤖 Tích hợp AI vào màn hình Chat — MyMoney

## Tổng quan

Màn hình `AIChatScreen` có giao diện chat (user bên phải, AI bên trái).
Khi người dùng nhắn "Bữa tối 20k", AI sẽ:
1. Phản hồi tin nhắn thân thiện bằng tiếng Việt (VD: "Oh tuyệt vời! Bữa tối chỉ có 20k 🎉")
2. Parse giao dịch từ tin nhắn và trả về JSON
3. Tự động lưu giao dịch vào Room database

---

## ✅ Phương án đang dùng: Groq API (Miễn phí)

### Tại sao chọn Groq?

| Tiêu chí | Google Gemini | **Groq (đang dùng)** |
|---|---|---|
| Giá | Miễn phí có giới hạn | ✅ **Miễn phí hoàn toàn** |
| Rate limit | 15 req/phút | ✅ **30 req/phút** |
| Quota/ngày | Dễ hết | ✅ **14,400 req/ngày** |
| Tốc độ | Trung bình | ✅ **Rất nhanh (Groq LPU chip)** |
| Model | gemini-2.0-flash | ✅ **llama-3.3-70b-versatile** |

### Bước 1: Thêm dependency

```toml
# gradle/libs.versions.toml
[versions]
ktor = "2.3.0"

[libraries]
ktor-client-android = { group = "io.ktor", name = "ktor-client-android", version.ref = "ktor" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.ktor.client.android)
implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
```

### Bước 2: Lấy API Key (Miễn phí)

1. Vào [https://console.groq.com/keys](https://console.groq.com/keys)
2. Đăng ký / đăng nhập miễn phí
3. Tạo API Key → copy key dạng `gsk_xxxxxxxxxxxx`
4. Thêm vào `local.properties` (**không commit file này lên Git**):
   ```
   GROQ_API_KEY=gsk_xxxxxxxxxxxx
   ```
5. Đọc key trong `app/build.gradle.kts`:
   ```kotlin
   val localProperties = java.util.Properties()
   rootProject.file("local.properties").inputStream().use { localProperties.load(it) }
   buildConfigField("String", "GROQ_API_KEY", "\"${localProperties["GROQ_API_KEY"] ?: ""}\"")
   ```

### Bước 3: GroqService (`data/remote/GroqService.kt`)

```kotlin
object GroqService {
    private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "llama-3.3-70b-versatile"  // Model mới nhất, miễn phí

    // Ktor HTTP client với JSON serialization
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun chat(userMessage: String): String {
        val apiKey = BuildConfig.GROQ_API_KEY

        val httpResponse = client.post(GROQ_URL) {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(ChatRequest(
                model = MODEL,
                messages = listOf(
                    Message(role = "system", content = SYSTEM_PROMPT),
                    Message(role = "user", content = userMessage)
                )
            ))
        }

        val rawBody = httpResponse.bodyAsText()

        // Xử lý lỗi HTTP trước khi parse
        if (!httpResponse.status.isSuccess()) {
            val err = runCatching { Json.decodeFromString<ErrorWrapper>(rawBody) }.getOrNull()
            throw Exception(err?.error?.message ?: "HTTP ${httpResponse.status.value}")
        }

        return Json.decodeFromString<ChatResponse>(rawBody)
            .choices.firstOrNull()?.message?.content
            ?: "Xin lỗi, mình chưa hiểu ý bạn. Thử lại nhé! 😊"
    }
}
```

**System Prompt** hướng dẫn AI trả về JSON giao dịch:
```
Bạn là trợ lý tài chính AI trong ứng dụng MyMoney.
- Phản hồi ngắn gọn, vui vẻ bằng tiếng Việt
- Trả JSON ở cuối: { "transactions": [{ "note", "amount", "type", "category" }] }
- "k" = ×1,000 | "tr" = ×1,000,000
- type: "expense" (mặc định) hoặc "income" (lương, thưởng...)
```

### Bước 4: AddTransactionViewModel

```kotlin
// Debounce 500ms + Guard chống spam request
private var isWaitingForAI = false
private var submitDebounceJob: Job? = null

private fun handleSubmit() {
    val noteText = _uiState.value.noteInput.trim()
    if (noteText.isBlank() || isWaitingForAI) return

    // Huỷ job cũ nếu nhấn liên tục trong 500ms
    submitDebounceJob?.cancel()
    submitDebounceJob = viewModelScope.launch {
        delay(500L)
        isWaitingForAI = true
        try {
            // Hiển thị bubble "đang nhập..."
            // Gọi GeminiService.chat(noteText)  ← dùng GroqService bên trong
            // Thay bubble bằng phản hồi thật
        } finally {
            isWaitingForAI = false
        }
    }
}
```

---

## Cấu trúc file

```
app/src/main/java/com/example/mymoney/
├── data/
│   └── remote/
│       └── GeminiService.kt       ✅ Đang dùng Groq API bên trong
├── presentation/
│   └── viewmodel/
│       └── addtransaction/
│           └── AddTransactionViewModel.kt  ✅ Debounce 500ms + isWaitingForAI guard
└── ui/
    └── addtransaction/
        └── AddTransactionScreen.kt         ✅ Giao diện chat hoàn chỉnh
```

---

## Bảo mật

- `local.properties` đã có trong `.gitignore` → API key **không bị commit** lên Git
- API key chỉ tồn tại trong `BuildConfig` lúc build, không hardcode trong source code
- Groq free tier không yêu cầu thẻ tín dụng

---

## Lưu ý kỹ thuật

1. **Debounce 500ms** — tránh gọi API khi user nhấn nút liên tục
2. **isWaitingForAI guard** — chặn hoàn toàn khi đang chờ AI trả lời
3. **Error handling** — parse lỗi HTTP từ Groq trước khi parse response thành công
4. **Lưu lịch sử chat** — hiện tại lưu trong memory (ViewModel). Có thể thêm Room table `chat_messages` nếu cần persist
5. **Model `llama-3.3-70b-versatile`** — model 70B tham số, hiểu tiếng Việt tốt, miễn phí hoàn toàn
