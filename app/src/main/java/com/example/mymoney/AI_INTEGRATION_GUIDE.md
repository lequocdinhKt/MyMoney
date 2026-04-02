# 🤖 Hướng dẫn tích hợp AI vào màn hình Chat

## Tổng quan

Màn hình `AIChatScreen` hiện tại đã có giao diện chat (user bên phải, AI bên trái).
Khi người dùng nhắn "Bữa tối 20k", AI sẽ:
1. Phản hồi tin nhắn thân thiện (VD: "Oh tuyệt vời! Bữa tối chỉ có 20k 🎉")
2. Parse giao dịch từ tin nhắn
3. Tự động lưu giao dịch vào Room database

**Hiện tại:** Chỉ có giao diện UI, chưa có AI. Dưới đây là các cách tích hợp.

---

## Phương án 1: Google Gemini API (Khuyến nghị — Miễn phí)

### Bước 1: Thêm dependency

```kotlin
// gradle/libs.versions.toml
[versions]
generativeai = "0.9.0"

[libraries]
google-generativeai = { group = "com.google.ai.client.generativeai", name = "generativeai", version.ref = "generativeai" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.google.generativeai)
```

### Bước 2: Lấy API Key

1. Vào [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Tạo API Key miễn phí
3. Thêm vào `local.properties`:
   ```
   GEMINI_API_KEY=your-api-key-here
   ```
4. Đọc key trong `build.gradle.kts`:
   ```kotlin
   android {
       defaultConfig {
           buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\"")
       }
       buildFeatures { buildConfig = true }
   }
   ```

### Bước 3: Tạo AI Service

```kotlin
// data/remote/GroqService.kt
package com.example.mymoney.data.remote

import com.google.ai.client.generativeai.GenerativeModel

object GeminiService {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        // System instruction để AI hiểu vai trò
        systemInstruction = content {
            text("""
                Bạn là trợ lý tài chính AI trong app MyMoney.
                Khi người dùng nhắn giao dịch (VD: "bữa tối 20k"), bạn phải:
                1. Phản hồi vui vẻ, ngắn gọn bằng tiếng Việt
                2. Trả về JSON ở cuối tin nhắn trong block ```json ... ```
                
                Format JSON:
                ```json
                {
                  "transactions": [
                    { "note": "Bữa tối", "amount": 20000, "type": "expense", "category": "Ăn uống" }
                  ]
                }
                ```
                
                Quy tắc:
                - "k" = 1,000 VNĐ (20k = 20000)
                - "tr" = 1,000,000 VNĐ (10tr = 10000000)  
                - Mặc định type = "expense", trừ khi rõ ràng là thu nhập (lương, thưởng...)
                - Tự suy luận category: Ăn uống, Di chuyển, Mua sắm, Giải trí, Thu nhập, Khác
            """.trimIndent())
        }
    )

    suspend fun chat(userMessage: String): String {
        val response = model.generateContent(userMessage)
        return response.text ?: "Xin lỗi, mình không hiểu. Bạn thử lại nhé!"
    }
}
```

### Bước 4: Cập nhật ViewModel

```kotlin
// Trong AddTransactionViewModel.kt — hàm handleSubmit()

private fun handleSubmit() {
    val noteText = _uiState.value.noteInput.trim()
    if (noteText.isBlank()) return

    // 1. Thêm tin nhắn người dùng
    val userMessage = ChatMessage(
        id = ++messageIdCounter,
        content = noteText,
        sender = ChatSender.USER
    )
    _uiState.update {
        it.copy(
            messages = it.messages + userMessage,
            noteInput = "",
            isEmpty = false
        )
    }

    // 2. Gọi AI và thêm phản hồi
    viewModelScope.launch {
        try {
            val aiResponse = GeminiService.chat(noteText)
            
            // Thêm tin nhắn AI
            val aiMessage = ChatMessage(
                id = ++messageIdCounter,
                content = aiResponse.substringBefore("```json"),  // Chỉ hiển thị text
                sender = ChatSender.AI
            )
            _uiState.update { it.copy(messages = it.messages + aiMessage) }

            // Parse JSON từ phản hồi AI → lưu giao dịch vào Room
            val jsonBlock = extractJsonBlock(aiResponse)
            if (jsonBlock != null) {
                val transactions = parseTransactions(jsonBlock)
                transactions.forEach { addTransactionUseCase(it) }
            }
        } catch (e: Exception) {
            val errorMessage = ChatMessage(
                id = ++messageIdCounter,
                content = "⚠️ Có lỗi xảy ra: ${e.message}",
                sender = ChatSender.AI
            )
            _uiState.update { it.copy(messages = it.messages + errorMessage) }
        }
    }
}

// Helper: Trích xuất JSON block từ phản hồi AI
private fun extractJsonBlock(response: String): String? {
    val regex = Regex("```json\\s*(.+?)\\s*```", RegexOption.DOT_MATCHES_ALL)
    return regex.find(response)?.groupValues?.get(1)
}

// Helper: Parse JSON thành danh sách TransactionModel
private fun parseTransactions(json: String): List<TransactionModel> {
    // Dùng kotlinx.serialization hoặc Gson để parse
    // TODO: Implement parsing logic
    return emptyList()
}
```

---

## Phương án 2: OpenAI API (ChatGPT)

### Dependency
```kotlin
// Dùng Ktor (đã có sẵn trong project) để gọi API
implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
```

### Service
```kotlin
object OpenAIService {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json() }
    }

    suspend fun chat(userMessage: String): String {
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            header("Authorization", "Bearer YOUR_API_KEY")
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "model" to "gpt-4o-mini",
                "messages" to listOf(
                    mapOf("role" to "system", "content" to "Bạn là trợ lý tài chính..."),
                    mapOf("role" to "user", "content" to userMessage)
                )
            ))
        }
        // Parse response...
        return "..."
    }
}
```

> ⚠️ OpenAI API không miễn phí. Gemini miễn phí 15 request/phút (đủ cho app cá nhân).

---

## Phương án 3: Supabase Edge Functions + AI

Nếu muốn giấu API key khỏi app (bảo mật hơn):

1. Tạo Edge Function trên Supabase Dashboard
2. Edge Function gọi Gemini/OpenAI server-side
3. App chỉ gọi Supabase Edge Function

```typescript
// supabase/functions/ai-chat/index.ts
import { GoogleGenerativeAI } from "@google/generative-ai";

Deno.serve(async (req) => {
    const { message } = await req.json();
    const genAI = new GoogleGenerativeAI(Deno.env.get("GEMINI_KEY")!);
    const model = genAI.getGenerativeModel({ model: "gemini-2.0-flash" });
    const result = await model.generateContent(message);
    return new Response(JSON.stringify({ reply: result.response.text() }));
});
```

---

## Cấu trúc file cần tạo/sửa

```
app/src/main/java/com/example/mymoney/
├── data/
│   └── remote/
│       └── GroqService.kt          ← NEW: Gọi Gemini API
├── presentation/
│   └── viewmodel/
│       └── addtransaction/
│           ├── AddTransactionViewModel.kt  ← EDIT: Thêm logic gọi AI
│           └── addtransaction/
│               └── AddTransactionContract.kt  ← DONE ✅ (đã có ChatMessage)
└── ui/
    └── addtransaction/
        └── AddTransactionScreen.kt    ← DONE ✅ (đã có giao diện chat)
```

---

## Lưu ý quan trọng

1. **KHÔNG lưu API key trong code** — dùng `local.properties` hoặc Supabase secrets
2. **Xử lý offline**: Khi không có mạng, hiển thị tin nhắn lỗi thân thiện
3. **Rate limiting**: Gemini free tier = 15 req/min. Nên thêm debounce
4. **Lưu lịch sử chat**: Hiện tại chat chỉ lưu trong memory. Có thể thêm Room table `chat_messages` nếu cần persist
5. **Streaming response**: Gemini hỗ trợ streaming — có thể hiển thị tin nhắn AI từng chữ (giống ChatGPT)
