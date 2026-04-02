package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.ChatMessageModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository cho tin nhắn chat với AI.
 * Chỉ lưu trong Room (không sync Supabase).
 * Tự động xóa sau 48h qua [deleteOldMessages].
 */
interface ChatRepository {
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageModel>>
    suspend fun saveMessage(message: ChatMessageModel): Long
    suspend fun deleteOldMessages()          // xóa tin nhắn > 48h
    suspend fun deleteSession(sessionId: String)
}
