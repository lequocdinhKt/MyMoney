package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymoney.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE user_id = :userId AND session_id = :sessionId ORDER BY created_at ASC")
    fun observeSession(userId: String, sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT DISTINCT session_id FROM chat_messages WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeSessionIds(userId: String): Flow<List<String>>

    /** Lấy toàn bộ tin nhắn của user, sắp xếp theo thời gian tăng dần */
    @Query("SELECT * FROM chat_messages WHERE user_id = :userId ORDER BY created_at ASC")
    fun observeMessagesByUser(userId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity): Long

    /** Xóa vĩnh viễn tin nhắn cũ hơn ngưỡng (dùng cho WorkManager 48h cleanup) */
    @Query("DELETE FROM chat_messages WHERE created_at < :thresholdMs")
    suspend fun deleteOlderThan(thresholdMs: Long)

    /** Xóa toàn bộ session */
    @Query("DELETE FROM chat_messages WHERE user_id = :userId AND session_id = :sessionId")
    suspend fun deleteSession(userId: String, sessionId: String)
}

