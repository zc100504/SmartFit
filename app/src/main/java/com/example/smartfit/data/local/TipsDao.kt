package com.example.smartfit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smartfit.data.model.TipMessage
import com.example.smartfit.data.model.TipThread
import kotlinx.coroutines.flow.Flow

@Dao
interface TipsDao {

    @Query("""
        SELECT * FROM tip_threads
        WHERE userId = :userId
        ORDER BY lastUpdatedAt DESC
    """)
    fun getThreadsForUser(userId: Long): Flow<List<TipThread>>

    @Query("""
        SELECT * FROM tip_messages
        WHERE threadId = :threadId
        ORDER BY timestamp ASC
    """)
    fun getMessagesForThread(threadId: Long): Flow<List<TipMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: TipThread): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: TipMessage)

    @Query("DELETE FROM tip_threads WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)

    @Query("""
        UPDATE tip_threads 
        SET preview = :preview, lastUpdatedAt = :updatedAt 
        WHERE id = :id
    """)
    suspend fun updatePreview(
        id: Long,
        preview: String,
        updatedAt: Long
    )

    @Query("DELETE FROM tip_threads WHERE id = :id")
    suspend fun deleteThreadById(id: Long)

    // 🔥 Random assistant tip for one user
    @Query("""
        SELECT tm.text 
        FROM tip_messages AS tm
        INNER JOIN tip_threads AS tt ON tm.threadId = tt.id
        WHERE tt.userId = :userId AND tm.author = 'ASSISTANT'
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getRandomAssistantTipForUser(userId: Long): String?
}

