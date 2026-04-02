package com.example.mymoney.domain.model

/**
 * Domain model cho tin nhắn chat với AI.
 * Chỉ lưu trong Room (không sync Supabase).
 * Tự động xóa sau 48h.
 */
data class ChatMessageModel(
    val id: Long = 0L,
    val userId: String,
    val content: String,
    val sender: String,             // "user" hoặc "ai"
    val sessionId: String,
    val transactionId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
