// app/src/main/java/com/example/smartfit/ui/tips/TipsViewModel.kt
package com.example.smartfit.ui.tips

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.model.TipMessage
import com.example.smartfit.data.model.TipThread
import com.example.smartfit.data.repository.PrefsRepository
import com.example.smartfit.data.repository.TipsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TipsViewModel(
    private val tipsRepo: TipsRepository,
    private val prefsRepo: PrefsRepository
) : ViewModel() {

    // region Logging helpers

    private val TAG = "TipsViewModel"

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

    // ----- Menu -----
    private val _menuUiState = MutableStateFlow(TipsMenuUiState())
    val menuUiState: StateFlow<TipsMenuUiState> = _menuUiState.asStateFlow()

    // ----- Conversation -----
    private val _conversationUiState = MutableStateFlow(TipConversationUiState())
    val conversationUiState: StateFlow<TipConversationUiState> =
        _conversationUiState.asStateFlow()

    private var currentUserId: Long? = null
    private var currentThreadId: Long? = null
    private var messagesJob: Job? = null

    // ⭐ 一次性事件 Flow
    private val _openThreadEvents = MutableSharedFlow<String>()
    val openThreadEvents: SharedFlow<String> = _openThreadEvents

    init {
        logD("init() TipsViewModel created, starting observeThreads()")
        observeThreads()
    }

    private fun observeThreads() {
        viewModelScope.launch {
            prefsRepo.getUserId()
                .distinctUntilChanged()
                .onEach { userId ->
                    logD("observeThreads() userId changed: $userId")
                }
                .flatMapLatest { userId ->
                    if (userId <= 0L) {
                        logD("observeThreads() no valid user, emitting empty threads")
                        currentUserId = null
                        flowOf(emptyList<TipThread>())
                    } else {
                        logD("observeThreads() loading threads for userId=$userId")
                        currentUserId = userId
                        tipsRepo.getThreadsForUser(userId)
                    }
                }
                .collect { threads ->
                    logD("observeThreads() received ${threads.size} threads")
                    _menuUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            threads = threads.map { it.toMenuUi() }
                        )
                    }
                }
        }
    }

    /** New question from NewTipQuestionScreen. */
    fun submitNewQuestion(question: String) {
        viewModelScope.launch {
            logD("submitNewQuestion() called, question length=${question.length}")

            val userId = currentUserId ?: prefsRepo.getUserId().firstOrNull() ?: -1L
            if (userId <= 0L) {
                logD("submitNewQuestion() aborted: invalid userId=$userId")
                return@launch
            }

            _menuUiState.update { it.copy(isLoading = true) }
            logD("submitNewQuestion() creating thread for userId=$userId")

            try {
                val threadId = tipsRepo.createThreadWithFirstMessage(
                    userId = userId,
                    userQuestion = question
                )
                logD("submitNewQuestion() createThreadWithFirstMessage() returned threadId=$threadId")

                _openThreadEvents.emit(threadId.toString())
                logD("submitNewQuestion() emitted openThreadEvents for threadId=$threadId")
            } catch (e: Exception) {
                logE("submitNewQuestion() error", e)
            } finally {
                _menuUiState.update { it.copy(isLoading = false) }
                logD("submitNewQuestion() finished, isLoading=false")
            }
        }
    }

    /** Open a thread (TipConversationScreen). */
    fun openThread(threadId: String) {
        logD("openThread() called with threadId=$threadId")
        val id = threadId.toLongOrNull()
        if (id == null) {
            logD("openThread() aborted: invalid threadId=$threadId")
            return
        }

        currentThreadId = id
        messagesJob?.cancel()
        logD("openThread() set currentThreadId=$id and cancelled previous messagesJob")

        val baseThreadUi = menuUiState.value.threads.find { it.id == threadId }
            ?: TipThreadUiState(
                id = threadId,
                title = "Tips",
                preview = "",
                messages = emptyList()
            )

        _conversationUiState.update {
            it.copy(
                isLoading = true,
                inputText = "",
                isSending = false,
                thread = baseThreadUi
            )
        }

        messagesJob = viewModelScope.launch {
            logD("openThread() start collecting messages for threadId=$id")
            tipsRepo.getMessagesForThread(id)
                .collect { msgs ->
                    logD("openThread() received ${msgs.size} messages for threadId=$id")
                    val msgUi = msgs.map { it.toUi() }
                    _conversationUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            thread = baseThreadUi.copy(messages = msgUi)
                        )
                    }
                }
        }
    }

    fun onInputChange(text: String) {
        logD("onInputChange() length=${text.length}")
        _conversationUiState.update { state ->
            state.copy(inputText = text)
        }
    }

    /** Follow-up question in current thread. */
    fun sendMessageInCurrentThread(
        userQuestion: String
    ) {
        logD("sendMessageInCurrentThread() called, question length=${userQuestion.length}")

        val threadId = currentThreadId
        if (threadId == null) {
            logD("sendMessageInCurrentThread() aborted: currentThreadId is null")
            return
        }

        val currentThread = _conversationUiState.value.thread
        if (currentThread == null) {
            logD("sendMessageInCurrentThread() aborted: currentThread is null")
            return
        }

        // 1. 先构建本地的用户消息 & 占位的 AI 消息
        val userMessage = TipMessageUiState(
            author = TipMessageAuthor.USER,
            text = userQuestion
        )

        val loadingMessage = TipMessageUiState(
            author = TipMessageAuthor.ASSISTANT,
            text = "Searching for tips...",
            isPlaceholder = true
        )

        // 2. 直接先更新 UI
        _conversationUiState.update { state ->
            state.copy(
                thread = currentThread.copy(
                    messages = currentThread.messages + userMessage + loadingMessage
                ),
                inputText = "",
                isSending = true
            )
        }
        logD("sendMessageInCurrentThread() appended user + placeholder messages, set isSending=true")

        // 3. 后台真正调用 API + 写数据库
        viewModelScope.launch {
            val userId = currentUserId ?: prefsRepo.getUserId().firstOrNull() ?: -1L
            if (userId <= 0L) {
                logD("sendMessageInCurrentThread() aborted in background: invalid userId=$userId")
                _conversationUiState.update { it.copy(isSending = false) }
                return@launch
            }

            try {
                logD("sendMessageInCurrentThread() calling tipsRepo.sendMessageInThread(userId=$userId, threadId=$threadId)")
                tipsRepo.sendMessageInThread(
                    userId = userId,
                    threadId = threadId,
                    userQuestion = userQuestion
                )
                logD("sendMessageInCurrentThread() sendMessageInThread() completed for threadId=$threadId")
            } catch (e: Exception) {
                logE("sendMessageInCurrentThread() error for threadId=$threadId", e)
            } finally {
                // DB Flow 会推送真正的 messages，覆盖掉上面的占位
                _conversationUiState.update { it.copy(isSending = false) }
                logD("sendMessageInCurrentThread() finished, isSending=false")
            }
        }
    }

    fun deleteTip(threadId: String) {
        logD("deleteTip() called with threadId=$threadId")
        val id = threadId.toLongOrNull()
        if (id == null) {
            logD("deleteTip() aborted: invalid threadId=$threadId")
            return
        }

        viewModelScope.launch {
            try {
                logD("deleteTip() calling tipsRepo.deleteThread(id=$id)")
                tipsRepo.deleteThread(id)
                logD("deleteTip() finished for id=$id (menu will auto-update via observeThreads())")
            } catch (e: Exception) {
                logE("deleteTip() error for id=$id", e)
            }
        }
    }

    // --- mapping helpers ---

    private fun TipThread.toMenuUi(): TipThreadUiState =
        TipThreadUiState(
            id = id.toString(),
            title = title,
            preview = preview,
            messages = emptyList()
        )

    private fun TipMessage.toUi(): TipMessageUiState =
        TipMessageUiState(
            author = if (author == "USER") TipMessageAuthor.USER else TipMessageAuthor.ASSISTANT,
            text = text,
            isPlaceholder = false
        )
}
