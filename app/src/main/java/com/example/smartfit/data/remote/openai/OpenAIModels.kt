// app/src/main/java/com/example/smartfit/data/remote/openai/OpenAIModels.kt
package com.example.smartfit.data.remote.openai

import com.squareup.moshi.Json

data class ChatMessage(
    val role: String,        // "system" / "user" / "assistant"
    val content: String
)

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7
)

data class ChatChoice(
    val index: Int,
    val message: ChatMessage
)

data class ChatUsage(
    @Json(name = "prompt_tokens") val promptTokens: Int?,
    @Json(name = "completion_tokens") val completionTokens: Int?,
    @Json(name = "total_tokens") val totalTokens: Int?
)

data class ChatResponse(
    val id: String,
    val choices: List<ChatChoice>,
    val usage: ChatUsage?
)
