package com.example.smartfit.ui.logs

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.model.LogItem
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.FoodRepository
import com.example.smartfit.data.repository.PrefsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LogDetailViewModel(
    private val activityRepository: ActivityRepository,
    private val foodRepository: FoodRepository,
    private val prefsRepository: PrefsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // region Logging helpers

    private val TAG = "LogDetailViewModel"

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

    // Route arguments (may be null when used from the tablet detail pane)
    private val routeId: Long? = savedStateHandle["id"]
    private val routeType: String? = savedStateHandle["type"]

    // Holds the currently displayed log (food or activity)
    private val _log = MutableStateFlow<LogItem?>(null)
    val log: StateFlow<LogItem?> = _log.asStateFlow()

    private val _deleteCompleted = MutableStateFlow(false)
    val deleteCompleted: StateFlow<Boolean> = _deleteCompleted.asStateFlow()

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()

    init {
        logD("init() called with routeId=$routeId, routeType=$routeType")
        // If we came from NavHost (Dest.LogDetail), arguments will be present.
        // In that case we can auto-load the log here.
        if (routeId != null && routeType != null) {
            logD("init() auto-loading log from route args")
            internalLoadLog(routeId, routeType)
        } else {
            logD("init() no route args, waiting for explicit loadLog()")
        }
    }

    /**
     * Public API so that other entry points (like the tablet detail pane)
     * can tell this ViewModel which log to show.
     */
    fun loadLog(id: Long, type: String) {
        logD("loadLog() called with id=$id, type=$type")
        internalLoadLog(id, type)
    }

    /**
     * Shared loading logic used by both the init{} route-based path
     * and the explicit loadLog(...) path.
     */
    private fun internalLoadLog(id: Long, type: String) {
        logD("internalLoadLog() start, id=$id, type=$type")
        viewModelScope.launch {
            try {
                // Get the current user id from preferences
                val userId = prefsRepository.getUserId().first()
                logD("internalLoadLog() userId from prefs=$userId")

                if (userId == -1L || userId == 0L) {
                    // User not logged in or invalid id; do nothing (or show error if you wish)
                    logD("internalLoadLog() aborted: invalid userId")
                    return@launch
                }

                val logFlow: Flow<LogItem?> = when (type) {
                    "food" -> {
                        logD("internalLoadLog() loading FoodLog from repository")
                        foodRepository.getById(id, userId)
                    }
                    "activity" -> {
                        logD("internalLoadLog() loading ActivityLog from repository")
                        activityRepository.getById(id, userId)
                    }
                    else -> {
                        logD("internalLoadLog() unknown type='$type', emitting null")
                        flowOf(null)
                    }
                }

                logFlow.collect { fetchedLog ->
                    logD(
                        "internalLoadLog() collected log: " +
                                when (fetchedLog) {
                                    is ActivityLog -> "ActivityLog(id=${fetchedLog.id}, title=${fetchedLog.title})"
                                    is FoodLog -> "FoodLog(id=${fetchedLog.id}, name=${fetchedLog.name})"
                                    null -> "null"
                                    else -> "Unknown type"
                                }
                    )
                    _log.value = fetchedLog
                }
            } catch (e: Exception) {
                logE("internalLoadLog() error for id=$id, type=$type", e)
            }
        }
    }

    fun onShowDeleteDialog() {
        logD("onShowDeleteDialog()")
        _showDeleteConfirmDialog.value = true
    }

    fun onDismissDeleteDialog() {
        logD("onDismissDeleteDialog()")
        _showDeleteConfirmDialog.value = false
    }

    fun confirmDeleteLog() {
        logD("confirmDeleteLog() called")
        onDismissDeleteDialog()
        val logToDelete = log.value
        if (logToDelete == null) {
            logD("confirmDeleteLog() aborted: log is null")
            return
        }

        viewModelScope.launch {
            try {
                when (logToDelete) {
                    is ActivityLog -> {
                        logD("confirmDeleteLog() deleting ActivityLog id=${logToDelete.id}")
                        activityRepository.delete(logToDelete)
                    }
                    is FoodLog -> {
                        logD("confirmDeleteLog() deleting FoodLog id=${logToDelete.id}")
                        foodRepository.delete(logToDelete)
                    }
                }
                _deleteCompleted.value = true
                logD("confirmDeleteLog() deleteCompleted set to true")
            } catch (e: Exception) {
                logE("confirmDeleteLog() error while deleting log", e)
            }
        }
    }

    fun onDeleteCompleted() {
        logD("onDeleteCompleted() resetting deleteCompleted=false")
        _deleteCompleted.value = false
    }
}
