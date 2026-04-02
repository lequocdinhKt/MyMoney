package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.ChatMessageDao
import com.example.mymoney.data.local.entity.ChatMessageEntity
import com.example.mymoney.domain.model.ChatMessageModel
import com.example.mymoney.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val dao: ChatMessageDao
) : ChatRepository {

    override fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageModel>> =
        dao.getMessagesBySession(sessionId).map { it.map { e -> e.toDomain() } }

    override fun getAllMessagesByUser(userId: String): Flow<List<ChatMessageModel>> =
        dao.getAllMessagesByUser(userId).map { it.map { e -> e.toDomain() } }

    override suspend fun getLatestSessionId(userId: String): String? =
        dao.getLatestSessionId(userId)

    override suspend fun saveMessage(message: ChatMessageModel): Long =
        dao.insertMessage(ChatMessageEntity.fromDomain(message))

    override suspend fun deleteOldMessages() {
        val cutoff = System.currentTimeMillis() - 48L * 60 * 60 * 1000
        dao.deleteOldMessages(cutoff)
    }

    override suspend fun deleteSession(sessionId: String) =
        dao.deleteSession(sessionId)
}
