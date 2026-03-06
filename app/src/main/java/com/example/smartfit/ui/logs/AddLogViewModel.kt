package com.example.smartfit.ui.logs

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.PrefsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- FIX 1: Add state for the confirmation dialog ---
data class AddLogUiState(
    val title: String = "",
    val type: String = "Running",
    val durationMin: String = "",
    val distanceKm: String = "",
    val calories: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val showUpdateConfirmation: Boolean = false // ADDED
)

sealed interface AddLogEffect {
    data object SaveCompleted : AddLogEffect
}

class AddLogViewModel(
    private val activityRepository: ActivityRepository,
    private val prefsRepository: PrefsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // region Logging helpers

    private val TAG = "AddLogViewModel"

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

    private val _state = MutableStateFlow(AddLogUiState())
    val state: StateFlow<AddLogUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddLogEffect>()
    val effect = _effect.asSharedFlow()

    private val logId: Long = savedStateHandle["logId"] ?: -1L

    private val userId: StateFlow<Long> = prefsRepository.getUserId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1L)

    val isFormValid: StateFlow<Boolean> = state.map { s ->
        s.title.isNotBlank() &&
                s.type.isNotBlank() &&
                s.durationMin.isNotBlank() &&
                s.calories.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = false
    )

    val isReady: StateFlow<Boolean> = combine(userId, state) { id, s ->
        val userIdIsLoaded = id != -1L
        val dataIsLoaded = if (s.isEditMode) !s.isLoading else true
        userIdIsLoaded && dataIsLoaded
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = false
    )

    init {
        if (logId != -1L) {
            logD("init() in EDIT mode, logId=$logId")
            _state.update { it.copy(isEditMode = true, isLoading = true) }
            viewModelScope.launch {
                val currentUserId = userId.first { it != -1L }
                logD("init() loading ActivityLog for userId=$currentUserId, logId=$logId")
                val log = activityRepository.getById(logId, currentUserId).firstOrNull()
                if (log != null) {
                    logD("init() ActivityLog loaded: title='${log.title}', type='${log.type}'")
                    _state.update {
                        it.copy(
                            isLoading = false, // crucial for 'isReady'
                            title = log.title ?: "",
                            notes = log.notes ?: "",
                            type = log.type,
                            durationMin = log.durationMin?.toString() ?: "",
                            distanceKm = log.distance?.toString() ?: "",
                            calories = log.calories?.toInt()?.toString() ?: ""
                        )
                    }
                } else {
                    logD("init() ActivityLog not found for id=$logId")
                    _state.update { it.copy(isLoading = false, error = "Log not found.") }
                }
            }
        } else {
            logD("init() in CREATE mode (new ActivityLog)")
        }
    }

    fun onTitleChange(title: String) {
        logD("onTitleChange() value='$title'")
        _state.update { it.copy(title = title, error = null) }
    }

    fun onTypeChange(type: String) {
        logD("onTypeChange() type='$type'")
        _state.update { it.copy(type = type, error = null) }
    }

    fun onDurationChange(duration: String) {
        val filtered = duration.filter { it.isDigit() }
        logD("onDurationChange() raw='$duration', filtered='$filtered'")
        _state.update { it.copy(durationMin = filtered, error = null) }
    }

    fun onDistanceChange(distance: String) {
        logD("onDistanceChange() value='$distance'")
        _state.update { it.copy(distanceKm = distance, error = null) }
    }

    fun onCaloriesChange(calories: String) {
        val filtered = calories.filter { it.isDigit() }
        logD("onCaloriesChange() raw='$calories', filtered='$filtered'")
        _state.update { it.copy(calories = filtered, error = null) }
    }

    fun onNotesChange(notes: String) {
        logD("onNotesChange() length=${notes.length}")
        _state.update { it.copy(notes = notes, error = null) }
    }

    // --- FIX 2: Add methods to manage the dialog ---
    fun onDismissUpdateDialog() {
        logD("onDismissUpdateDialog()")
        _state.update { it.copy(showUpdateConfirmation = false) }
    }

    // --- FIX 3: Modify the save() method to show the dialog ---
    fun save() {
        val s = _state.value
        logD("save() called, isEditMode=${s.isEditMode}, isSaving=${s.isSaving}, isFormValid=${isFormValid.value}, isReady=${isReady.value}")

        if (!isFormValid.value || !isReady.value || s.isSaving) {
            logD("save() aborted: invalid state / form / already saving")
            return
        }

        // If in edit mode, show confirmation dialog and stop
        if (s.isEditMode) {
            logD("save() in EDIT mode, showing confirmation dialog")
            _state.update { it.copy(showUpdateConfirmation = true) }
            return
        }

        // If not in edit mode, proceed with saving a new log
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val currentUserId = userId.value
            if (currentUserId == -1L) {
                logD("save() aborted: user not logged in")
                _state.update { it.copy(isSaving = false, error = "Cannot save log. User is not logged in.") }
                return@launch
            }

            val sNow = _state.value
            logD("save() creating NEW ActivityLog for userId=$currentUserId")

            val newLog = ActivityLog(
                userId = currentUserId,
                timestamp = System.currentTimeMillis(),
                type = sNow.type,
                title = sNow.title.takeIf { it.isNotBlank() },
                notes = sNow.notes.takeIf { it.isNotBlank() },
                durationMin = sNow.durationMin.toIntOrNull(),
                distance = sNow.distanceKm.toDoubleOrNull(),
                calories = sNow.calories.toDoubleOrNull(),
            )

            try {
                activityRepository.insert(newLog)
                logD("save() insert() success for title='${newLog.title}'")
                _effect.emit(AddLogEffect.SaveCompleted)
                _state.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                logE("save() insert() error", e)
                _state.update { it.copy(isSaving = false, error = "Failed to save log.") }
            }
        }
    }

    // --- FIX 4: Create a new function to handle the confirmed update ---
    fun confirmUpdate() = viewModelScope.launch {
        logD("confirmUpdate() called")
        if (!isFormValid.value || !isReady.value) {
            logD("confirmUpdate() aborted: form not valid or not ready")
            return@launch
        }

        val s = _state.value
        if (s.isSaving) {
            logD("confirmUpdate() aborted: already saving")
            return@launch
        }

        // Dismiss the dialog and set isSaving to true
        _state.update { it.copy(showUpdateConfirmation = false, isSaving = true) }

        val currentUserId = userId.value
        val duration = s.durationMin.toIntOrNull()
        val calories = s.calories.toDoubleOrNull()
        val distance = s.distanceKm.toDoubleOrNull()

        logD("confirmUpdate() fetching original ActivityLog for id=$logId, userId=$currentUserId")

        try {
            activityRepository.getById(logId, currentUserId).firstOrNull()?.let { originalLog ->
                logD("confirmUpdate() original ActivityLog found, updating...")
                val updatedLog = originalLog.copy(
                    title = s.title.takeIf { it.isNotBlank() },
                    notes = s.notes.takeIf { it.isNotBlank() },
                    type = s.type,
                    durationMin = duration,
                    distance = distance,
                    calories = calories
                )
                activityRepository.update(updatedLog)
                logD("confirmUpdate() update() success for id=$logId")
            } ?: run {
                logD("confirmUpdate() original ActivityLog NOT found for id=$logId")
                _state.update { it.copy(error = "Log not found.") }
            }

            _effect.emit(AddLogEffect.SaveCompleted)
            _state.update { it.copy(isSaving = false) }
        } catch (e: Exception) {
            logE("confirmUpdate() update() error for id=$logId", e)
            _state.update { it.copy(isSaving = false, error = "Failed to update log.") }
        }
    }
}
