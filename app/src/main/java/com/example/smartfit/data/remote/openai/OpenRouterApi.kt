// app/src/main/java/com/example/smartfit/data/remote/openrouter/OpenRouterApi.kt
package com.example.smartfit.data.remote.openai

import com.example.smartfit.data.remote.openai.ChatRequest
import com.example.smartfit.data.remote.openai.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {

    @POST("v1/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authHeader: String,                // "Bearer sk-or-xxxx"
        @Header("HTTP-Referer") referer: String = "https://smartfit.app", // 随便一个你的“网站”
        @Header("X-Title") appName: String = "SmartFit Android",   // 应用名字
        @Body request: ChatRequest
    ): ChatResponse
}
