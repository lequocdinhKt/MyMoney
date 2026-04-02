package com.example.mymoney.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mymoney.domain.model.ChatMessageModel

/**
 * Room Entity cho bảng "chat_messages".
 * Lưu lịch sử chat với AI — tự động xóa sau 48h (do WorkManager hoặc query filter).
 */
@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["sessionId", "timestamp"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val content: String,
    val sender: String,             // "user" hoặc "ai"
    val sessionId: String,
    val transactionId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): ChatMessageModel = ChatMessageModel(
        id = id,
        userId = userId,
        content = content,
        sender = sender,
        sessionId = sessionId,
        transactionId = transactionId,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(model: ChatMessageModel): ChatMessageEntity = ChatMessageEntity(
            id = model.id,
            userId = model.userId,
            content = model.content,
            sender = model.sender,
            sessionId = model.sessionId,
            transactionId = model.transactionId,
            timestamp = model.timestamp
        )
    }
}
