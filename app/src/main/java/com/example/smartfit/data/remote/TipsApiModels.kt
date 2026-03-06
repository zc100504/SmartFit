// app/src/main/java/com/example/smartfit/data/remote/TipsApiModels.kt
package com.example.smartfit.data.remote

data class TipsRequest(
    val question: String,
    val dailySummary: String?,
    val weeklySummary: String?
)

data class TipsResponse(
    val answer: String
)
