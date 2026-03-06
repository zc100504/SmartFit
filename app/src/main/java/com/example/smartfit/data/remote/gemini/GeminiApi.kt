package com.example.smartfit.data.remote.gemini

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GeminiApi {

    @POST("models/gemini-2.0-flash-lite:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body body: GeminiRequest
    ): GeminiResponse
}
