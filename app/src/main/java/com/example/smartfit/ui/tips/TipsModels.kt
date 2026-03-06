// app/src/main/java/com/example/smartfit/ui/tips/TipsModels.kt
package com.example.smartfit.ui.tips

import java.time.Instant
import java.util.UUID

enum class TipMessageAuthor {
    USER, ASSISTANT
}

data class TipMessageUiState(
    val id: String = UUID.randomUUID().toString(),
    val author: TipMessageAuthor,
    val text: String,
    val timestamp: Instant = Instant.now(),
    val isPlaceholder: Boolean = false
)

data class TipThreadUiState(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val preview: String,                // short description for card
    val lastUpdated: Instant = Instant.now(),
    val messages: List<TipMessageUiState> = emptyList()
)

data class TipsMenuUiState(
    val isLoading: Boolean = false,
    val threads: List<TipThreadUiState> = emptyList()
)

data class TipConversationUiState(
    val isLoading: Boolean = false,
    val thread: TipThreadUiState? = null,
    val inputText: String = "",
    val isSending: Boolean = false
)
