// app/src/main/java/com/example/smartfit/data/repository/TipsRepository.kt
package com.example.smartfit.data.repository

import android.util.Log
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.local.TipsDao
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.model.TipMessage
import com.example.smartfit.data.model.TipThread
import com.example.smartfit.data.remote.gemini.GeminiApi
import com.example.smartfit.data.remote.gemini.GeminiContent
import com.example.smartfit.data.remote.gemini.GeminiPart
import com.example.smartfit.data.remote.gemini.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TipsRepository(
    private val dao: TipsDao,
    private val gemini: GeminiApi,
    private val foodRepo: FoodRepository,
    private val activityRepo: ActivityRepository,
) {

    // region Logging helpers

    private val TAG = "TipsRepository"

    private fun logD(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    private fun logE(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }
    }

    // endregion

    // region Read

    fun getThreadsForUser(userId: Long): Flow<List<TipThread>> {
        logD("getThreadsForUser() userId=$userId")
        return dao.getThreadsForUser(userId)
    }

    fun getMessagesForThread(threadId: Long): Flow<List<TipMessage>> {
        logD("getMessagesForThread() threadId=$threadId")
        return dao.getMessagesForThread(threadId)
    }

    // endregion

    // region Write / business logic

    suspend fun createThreadWithFirstMessage(
        userId: Long,
        userQuestion: String
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        logD("createThreadWithFirstMessage() start, userId=$userId, question='${userQuestion.take(60)}'")

        // 1. 取当天 & 最近 7 天数据
        val allFoods = foodRepo.getAllByUser(userId).first()
        val allActivities = activityRepo.getAllByUser(userId).first()

        logD("Loaded user data: allFoods=${allFoods.size}, allActivities=${allActivities.size}")

        val todayFoods = allFoods.filter { isSameDay(it.timestamp, now) }
        val todayActivities = allActivities.filter { isSameDay(it.timestamp, now) }

        val weekFoods = allFoods.filter { isWithinLastDays(it.timestamp, now, 7) }
        val weekActivities = allActivities.filter { isWithinLastDays(it.timestamp, now, 7) }

        logD(
            "Filtered logs: " +
                    "todayFoods=${todayFoods.size}, todayActivities=${todayActivities.size}, " +
                    "weekFoods=${weekFoods.size}, weekActivities=${weekActivities.size}"
        )

        val dailySummary = buildDailySummaryText(todayFoods, todayActivities)
        val weeklySummary = buildWeeklySummaryText(weekFoods, weekActivities)

        // 2. 调 Gemini 拿答案
        logD("Calling fetchAnswerFromGemini() for new thread...")
        val answerText = fetchAnswerFromGemini(
            question = userQuestion,
            dailySummary = dailySummary,
            weeklySummary = weeklySummary
        )
        logD("Gemini answer received (length=${answerText.length})")

        // 3. 存 thread
        val title = buildTitleFromQuestion(userQuestion)
        val thread = TipThread(
            userId = userId,
            title = title,
            preview = answerText.take(80),
            createdAt = now,
            lastUpdatedAt = now
        )
        logD("Inserting new TipThread for userId=$userId, title='${title.take(40)}'")
        val threadId = dao.insertThread(thread)
        logD("Inserted TipThread with id=$threadId")

        // 4. 存两条 message
        val userMsg = TipMessage(
            threadId = threadId,
            author = "USER",
            text = userQuestion,
            timestamp = now
        )
        val answerMsg = TipMessage(
            threadId = threadId,
            author = "ASSISTANT",
            text = answerText,
            timestamp = now + 1
        )

        logD("Inserting first user message for threadId=$threadId")
        dao.insertMessage(userMsg)
        logD("Inserting first assistant message for threadId=$threadId")
        dao.insertMessage(answerMsg)

        logD("createThreadWithFirstMessage() finished for threadId=$threadId")

        threadId
    }

    suspend fun sendMessageInThread(
        userId: Long,
        threadId: Long,
        userQuestion: String
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        logD("sendMessageInThread() start, userId=$userId, threadId=$threadId, question='${userQuestion.take(60)}'")

        val allFoods = foodRepo.getAllByUser(userId).first()
        val allActivities = activityRepo.getAllByUser(userId).first()

        logD("Loaded user data (existing thread): allFoods=${allFoods.size}, allActivities=${allActivities.size}")

        val todayFoods = allFoods.filter { isSameDay(it.timestamp, now) }
        val todayActivities = allActivities.filter { isSameDay(it.timestamp, now) }

        val weekFoods = allFoods.filter { isWithinLastDays(it.timestamp, now, 7) }
        val weekActivities = allActivities.filter { isWithinLastDays(it.timestamp, now, 7) }

        logD(
            "Filtered logs (existing thread): " +
                    "todayFoods=${todayFoods.size}, todayActivities=${todayActivities.size}, " +
                    "weekFoods=${weekFoods.size}, weekActivities=${weekActivities.size}"
        )

        val dailySummary = buildDailySummaryText(todayFoods, todayActivities)
        val weeklySummary = buildWeeklySummaryText(weekFoods, weekActivities)

        logD("Calling fetchAnswerFromGemini() for existing threadId=$threadId...")
        val answerText = fetchAnswerFromGemini(
            question = userQuestion,
            dailySummary = dailySummary,
            weeklySummary = weeklySummary
        )
        logD("Gemini answer received for threadId=$threadId (length=${answerText.length})")

        val userMsg = TipMessage(
            threadId = threadId,
            author = "USER",
            text = userQuestion,
            timestamp = now
        )
        val answerMsg = TipMessage(
            threadId = threadId,
            author = "ASSISTANT",
            text = answerText,
            timestamp = now + 1
        )

        logD("Inserting user follow-up message for threadId=$threadId")
        dao.insertMessage(userMsg)
        logD("Inserting assistant follow-up message for threadId=$threadId")
        dao.insertMessage(answerMsg)

        logD("Updating thread preview for threadId=$threadId")
        updateThreadPreview(threadId, answerText, now + 1)

        logD("sendMessageInThread() finished for threadId=$threadId")
    }

    suspend fun clearAllTipsForUser(userId: Long) = withContext(Dispatchers.IO) {
        logD("clearAllTipsForUser() for userId=$userId")
        dao.deleteAllForUser(userId)
        logD("All tips deleted for userId=$userId")
    }

    // endregion

    // region Gemini 调用
    private val geminiApiKey: String = BuildConfig.GEMINI_API_KEY

    private suspend fun fetchAnswerFromGemini(
        question: String,
        dailySummary: String?,
        weeklySummary: String?
    ): String {

        logD("fetchAnswerFromGemini() called, question length=${question.length}")
        logD("GEMINI_API_KEY length = ${geminiApiKey.length}")

        if (geminiApiKey.isBlank()) {
            logE("Gemini API key is blank, returning fallback message")
            return "Gemini API key is not configured. Please set it in local.properties as GEMINI_API_KEY."
        }

        val prompt = buildString {
            appendLine("You are a helpful fitness and nutrition coach for a mobile app called SmartFit.")
            appendLine("Use the user's logs to give specific, practical advice.")
            appendLine()
            appendLine("User question:")
            appendLine(question)
            appendLine()

            if (!dailySummary.isNullOrBlank()) {
                appendLine("Today's logs:")
                appendLine(dailySummary)
                appendLine()
            }

            if (!weeklySummary.isNullOrBlank()) {
                appendLine("Weekly logs:")
                appendLine(weeklySummary)
                appendLine()
            }

            appendLine("Please answer in clear, friendly English.")
            appendLine("Do NOT use markdown or formatting.")
            appendLine("Do NOT use bullet points, bold, italics, or special characters like *, _, or **.")
            appendLine("Respond with plain text only, in paragraph form.")
            appendLine("Do NOT generate headings or sections.")
            appendLine("Do NOT mention that you are an AI model.")
            appendLine("If user ask any question not relevant to fitness, please reply don't understand question not relevant to fitness.")
        }

        logD("Built Gemini prompt, length=${prompt.length}")

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt)
                        )
                    )
                )
            )

            logD("Sending request to Gemini API...")
            val response = gemini.generateContent(
                apiKey = geminiApiKey,
                body = request
            )
            logD("Gemini API response received")

            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.takeIf { it.isNotBlank() }

            if (text == null) {
                logE("Gemini response text is null or blank, using fallback tip")
            } else {
                logD("Gemini response text length=${text.length}")
            }

            text ?: "Here is a general tip: stay active, sleep well, and keep consistent with your workouts."
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                logE("Gemini error body: $errorBody")
            }
            logE("Gemini error (Ask Gemini)", e)
            "I am having trouble contacting the server right now, but in general: " +
                    "start with light activity, keep good posture, and listen to your body."
        }
    }

    // endregion

    // region 文本 summary

    private fun buildDailySummaryText(
        foods: List<FoodLog>,
        activities: List<ActivityLog>
    ): String {
        logD("buildDailySummaryText() foods=${foods.size}, activities=${activities.size}")

        val sb = StringBuilder()

        sb.appendLine("Daily logs")
        sb.appendLine()
        sb.appendLine("Food:")

        foods.forEachIndexed { index, food ->
            sb.appendLine(
                "${index + 1}. Name: ${food.name}, " +
                        "Calories: ${food.calories} kcal, " +
                        "Time: ${formatTimestamp(food.timestamp)}"
            )
        }

        sb.appendLine()
        sb.appendLine("Activity:")

        activities.forEachIndexed { index, act ->
            val calPart = act.calories?.let { ", Calories burned: $it kcal" } ?: ""
            sb.appendLine(
                "${index + 1}. Name: ${act.title}, " +
                        "Duration: ${act.durationMin} min$calPart, " +
                        "Time: ${formatTimestamp(act.timestamp)}"
            )
        }

        return sb.toString()
    }

    private fun buildWeeklySummaryText(
        weekFoods: List<FoodLog>,
        weekActivities: List<ActivityLog>
    ): String {
        logD("buildWeeklySummaryText() weekFoods=${weekFoods.size}, weekActivities=${weekActivities.size}")

        val sb = StringBuilder()

        sb.appendLine("Weekly logs (last 7 days)")
        sb.appendLine()
        sb.appendLine("Food:")

        weekFoods.forEachIndexed { index, food ->
            sb.appendLine(
                "${index + 1}. Name: ${food.name}, " +
                        "Calories: ${food.calories} kcal, " +
                        "DateTime: ${formatTimestamp(food.timestamp)}"
            )
        }

        sb.appendLine()
        sb.appendLine("Activity:")

        weekActivities.forEachIndexed { index, act ->
            val calPart = act.calories?.let { ", Calories burned: $it kcal" } ?: ""
            sb.appendLine(
                "${index + 1}. Name: ${act.title}, " +
                        "Duration: ${act.durationMin} min$calPart, " +
                        "DateTime: ${formatTimestamp(act.timestamp)}"
            )
        }

        return sb.toString()
    }

    private fun formatTimestamp(ts: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(java.util.Date(ts))
    }

    // endregion

    // region 其他小工具

    private fun buildTitleFromQuestion(question: String): String {
        val trimmed = question.trim()
        if (trimmed.length <= 30) return trimmed
        return trimmed.take(27) + "..."
    }

    suspend fun deleteThread(threadId: Long) = withContext(Dispatchers.IO) {
        logD("deleteThread() threadId=$threadId")
        dao.deleteThreadById(threadId)
        logD("deleteThread() finished for threadId=$threadId")
    }

    private suspend fun updateThreadPreview(
        threadId: Long,
        latestAnswer: String,
        updatedAt: Long
    ) {
        logD(
            "updateThreadPreview() threadId=$threadId, " +
                    "answerPreview='${latestAnswer.take(40)}', updatedAt=$updatedAt"
        )

        dao.updatePreview(
            id = threadId,
            preview = latestAnswer.take(80),
            updatedAt = updatedAt
        )

        logD("updateThreadPreview() finished for threadId=$threadId")
    }

    private fun isSameDay(timestamp: Long, now: Long): Boolean {
        val oneDay = TimeUnit.DAYS.toMillis(1)
        return (now - timestamp) < oneDay
    }

    private fun isWithinLastDays(timestamp: Long, now: Long, days: Int): Boolean {
        val range = TimeUnit.DAYS.toMillis(days.toLong())
        return timestamp >= now - range
    }

    suspend fun getRandomTipForUser(userId: Long): String = withContext(Dispatchers.IO) {
        logD("getRandomTipForUser() userId=$userId")
        val tip = dao.getRandomAssistantTipForUser(userId)
        logD("getRandomTipForUser() result isNull=${tip == null}")
        tip ?: "Log your meals and activities daily to get more accurate insights."
    }

    // endregion
}
