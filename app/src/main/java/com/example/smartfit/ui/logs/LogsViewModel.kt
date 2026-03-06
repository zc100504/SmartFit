package com.example.smartfit.ui.logs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.model.LogItem
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.FoodRepository
import com.example.smartfit.data.repository.PrefsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * UI state for the Logs screen.
 */
data class LogsUiState(
    val items: List<LogItem> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel to retrieve all logs for the currently logged-in user.
 */
class LogsViewModel(
    private val activityRepository: ActivityRepository,
    private val foodRepository: FoodRepository,
    private val prefsRepository: PrefsRepository
) : ViewModel() {

    // region Logging helpers

    private val TAG = "LogsViewModel"

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

    /**
     * Holds the UI state for the logs screen.
     * It observes the current userId and fetches logs accordingly.
     */
    @OptIn(ExperimentalCoroutinesApi::class) // Needed for flatMapLatest
    val state: StateFlow<LogsUiState> =
        // Start the flow by getting the current user ID from preferences.
        prefsRepository.getUserId()
            .onEach { userId ->
                logD("userId flow emitted: $userId")
            }
            .flatMapLatest { userId ->
                // flatMapLatest automatically re-executes this block when userId changes.

                // --- FIX 1: Check for -1L instead of null ---
                if (userId == -1L) {
                    logD("No user logged in, emitting empty LogsUiState")
                    // If no user is logged in, emit a state with an empty list.
                    flowOf(LogsUiState(items = emptyList(), isLoading = false))
                } else {
                    logD("Loading logs for userId=$userId")
                    // If a user is logged in, combine the flows for their specific logs.
                    combine(
                        // --- FIX 2: Use the correct repository method names ---
                        activityRepository.getAllByUser(userId),
                        foodRepository.getAllByUser(userId)
                    ) { activities, foodLogs ->
                        logD("Combine logs: activities=${activities.size}, foodLogs=${foodLogs.size}")
                        // Combine, sort, and map the results to the UI state.
                        val combinedList =
                            (activities + foodLogs).sortedByDescending { it.timestamp }
                        logD("Combined list size=${combinedList.size}")
                        LogsUiState(items = combinedList, isLoading = false)
                    }.catch { e ->
                        logE("Error loading logs for userId=$userId", e)
                        emit(LogsUiState(items = emptyList(), isLoading = false))
                    }
                }
            }
            // Convert the resulting flow into a StateFlow for the UI.
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                // The initial state while waiting for the first user ID and data.
                initialValue = LogsUiState(isLoading = true)
            )
}
