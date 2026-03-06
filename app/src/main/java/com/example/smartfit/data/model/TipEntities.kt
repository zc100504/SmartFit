package com.example.smartfit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one conversation (one “topic” of tips), for example:
 * - "Evening Stretch"
 * - "Office Posture"
 *
 * Shown in the Tips menu grid as one card.
 */
@Entity(tableName = "tip_threads", indices = [Index("userId")])
data class TipThread(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val title: String,
    val preview: String,
    val createdAt: Long,
    val lastUpdatedAt: Long
)

@Entity(
    tableName = "tip_messages",
    foreignKeys = [
        ForeignKey(
            entity = TipThread::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("threadId")]
)
data class TipMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val threadId: Long,
    val author: String,  // "USER" / "ASSISTANT"
    val text: String,
    val timestamp: Long
)

