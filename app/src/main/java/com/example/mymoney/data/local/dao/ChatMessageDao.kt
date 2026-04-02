package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymoney.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    /** Lấy tin nhắn của 1 session, sắp xếp theo thời gian tăng dần */
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>>

    /**
     * Lấy danh sách session IDs của user, sắp xếp theo tin nhắn mới nhất.
     * Dùng GROUP BY + MAX() thay vì DISTINCT để tránh lỗi aggregate.
     */
    @Query("""
        SELECT sessionId FROM chat_messages
        WHERE userId = :userId
        GROUP BY sessionId
        ORDER BY MAX(timestamp) DESC
    """)
    fun getSessionIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(entity: ChatMessageEntity): Long

    /** Xóa tin nhắn cũ hơn 48h — gọi bởi WorkManager hoặc khi mở app */
    @Query("DELETE FROM chat_messages WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldMessages(cutoffTimestamp: Long)

    /** Xóa toàn bộ 1 session */
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
