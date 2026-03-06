// app/src/main/java/com/example/smartfit/data/remote/gemini/GeminiModels.kt
package com.example.smartfit.data.remote.gemini

/**
 * 对应 Gemini REST API 的 JSON 结构
 * 参考：POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
 */

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)
