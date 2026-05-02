package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.ChatMessageDao
import com.example.mymoney.data.local.entity.ChatMessageEntity
import com.example.mymoney.domain.model.ChatMessageModel
import com.example.mymoney.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class ChatRepositoryImpl(
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {

    override fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageModel>> =
        chatMessageDao.observeSession("", sessionId).map { list -> list.map { it.toModel() } }

    override fun getAllMessagesByUser(userId: String): Flow<List<ChatMessageModel>> =
        chatMessageDao.observeMessagesByUser(userId).map { list -> list.map { it.toModel() } }

    override suspend fun getLatestSessionId(userId: String): String? =
        chatMessageDao.observeSessionIds(userId).first().firstOrNull()

    override suspend fun saveMessage(message: ChatMessageModel): Long =
        chatMessageDao.insert(message.toEntity())

    override suspend fun deleteOldMessages() {
        val threshold = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(48)
        chatMessageDao.deleteOlderThan(threshold)
    }

    override suspend fun deleteSession(sessionId: String) =
        chatMessageDao.deleteSession("", sessionId)

    // ── Mappers ──

    private fun ChatMessageEntity.toModel() = ChatMessageModel(
        id            = id,
        userId        = userId,
        content       = content,
        sender        = sender,
        sessionId     = sessionId,
        transactionId = transactionId,
        timestamp     = createdAt
    )

    private fun ChatMessageModel.toEntity() = ChatMessageEntity(
        id            = id,
        userId        = userId,
        sessionId     = sessionId,
        sender        = sender,
        content       = content,
        transactionId = transactionId,
        createdAt     = timestamp
    )
}

