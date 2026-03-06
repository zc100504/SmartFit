package com.example.smartfit.ui.logs

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.repository.FoodRepository
import com.example.smartfit.data.repository.PrefsRepository
import com.example.smartfit.ui.navigation.Dest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

// --- FIX 1: Add state for the confirmation dialog ---
data class AddFoodUiState(
    val foodName: String = "",
    val calories: String = "",
    val mealType: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val isMealTypeMenuExpanded: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showUpdateConfirmation: Boolean = false // ADDED
)

class AddFoodViewModel(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: FoodRepository,
    private val prefsRepository: PrefsRepository
) : ViewModel() {

    // region Logging helpers

    private val TAG = "AddFoodViewModel"

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

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    val isFormValid: StateFlow<Boolean> = combine(
        uiState
    ) { (state) ->
        state.foodName.isNotBlank() &&
                state.calories.isNotBlank() &&
                (state.calories.toIntOrNull() ?: 0) > 0 &&
                state.mealType.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = false
    )

    private val logId: Long =
        savedStateHandle.get<Long>(Dest.AddFoodLog::logId.name) ?: -1L

    private val userId: StateFlow<Long> = prefsRepository.getUserId()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = -1L
        )

    val isReady: StateFlow<Boolean> = combine(
        userId,
        uiState
    ) { id, state ->
        val userIdIsLoaded = id != -1L
        val dataIsLoaded = if (state.isEditMode) !state.isLoading else true
        userIdIsLoaded && dataIsLoaded
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = false
    )

    init {
        if (logId != -1L) {
            logD("init() in EDIT mode, logId=$logId")
            _uiState.update { it.copy(isEditMode = true, isLoading = true) }
            viewModelScope.launch {
                val currentUserId = userId.first { it != -1L }
                logD("init() loading existing FoodLog for userId=$currentUserId, logId=$logId")
                val existingLog = foodRepository.getById(logId, currentUserId).firstOrNull()

                if (existingLog != null) {
                    logD("init() existing FoodLog loaded: name='${existingLog.name}', calories=${existingLog.calories}")
                    _uiState.update { currentState ->
                        currentState.copy(
                            foodName = existingLog.name,
                            calories = existingLog.calories?.toInt()?.toString() ?: "",
                            mealType = existingLog.mealType,
                            description = existingLog.notes ?: "",
                            isLoading = false
                        )
                    }
                } else {
                    logD("init() FoodLog not found for id=$logId")
                    _uiState.update { it.copy(isLoading = false, error = "Log not found") }
                }
            }
        } else {
            logD("init() in CREATE mode (new FoodLog)")
        }
    }

    fun onDescriptionChange(description: String) {
        logD("onDescriptionChange() length=${description.length}")
        _uiState.update { it.copy(description = description) }
    }

    fun onFoodNameChange(foodName: String) {
        logD("onFoodNameChange() value='$foodName'")
        _uiState.update { it.copy(foodName = foodName) }
    }

    fun onCaloriesChange(calories: String) {
        val digitsOnly = calories.filter { char -> char.isDigit() }
        logD("onCaloriesChange() raw='$calories', filtered='$digitsOnly'")
        _uiState.update { it.copy(calories = digitsOnly) }
    }

    fun onMealTypeChange(mealType: String) {
        logD("onMealTypeChange() mealType='$mealType'")
        _uiState.update { it.copy(mealType = mealType, isMealTypeMenuExpanded = false) }
    }

    fun onMealTypeMenuDismiss() {
        logD("onMealTypeMenuDismiss()")
        _uiState.update { it.copy(isMealTypeMenuExpanded = false) }
    }

    fun onMealTypeMenuOpen() {
        logD("onMealTypeMenuOpen()")
        _uiState.update { it.copy(isMealTypeMenuExpanded = true) }
    }

    // --- FIX 2: Add methods to manage the dialog ---
    fun onDismissUpdateDialog() {
        logD("onDismissUpdateDialog()")
        _uiState.update { it.copy(showUpdateConfirmation = false) }
    }

    // --- FIX 3: Modify saveFoodLog() to show the dialog ---
    fun saveFoodLog() {
        logD("saveFoodLog() called")
        if (!isFormValid.value) {
            logD("saveFoodLog() aborted: form not valid")
            return
        }

        val s = _uiState.value

        // If in edit mode, show the confirmation dialog and stop
        if (s.isEditMode) {
            logD("saveFoodLog() in EDIT mode, showing confirmation dialog")
            _uiState.update { it.copy(showUpdateConfirmation = true) }
            return
        }

        // --- This part is for saving a NEW log ---
        viewModelScope.launch {
            val currentUserId = userId.value
            if (currentUserId == -1L) {
                logD("saveFoodLog() aborted: user not logged in")
                _uiState.update { it.copy(error = "User not logged in.") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true) }
            logD("saveFoodLog() creating NEW FoodLog for userId=$currentUserId")

            val newLog = FoodLog(
                name = s.foodName.trim(),
                calories = s.calories.toDoubleOrNull(),
                mealType = s.mealType.trim(),
                timestamp = System.currentTimeMillis(),
                notes = s.description.trim().takeIf { it.isNotEmpty() } ?: "",
                userId = currentUserId
            )

            try {
                foodRepository.insert(newLog)
                logD("saveFoodLog() insert() success for food='${newLog.name}'")
                _uiState.update { it.copy(isSaving = false, saveCompleted = true) }
            } catch (e: Exception) {
                logE("saveFoodLog() insert() error", e)
                _uiState.update { it.copy(isSaving = false, error = "Failed to save log.") }
            }
        }
    }

    // --- FIX 4: Create a new function to handle the confirmed update ---
    fun confirmUpdate() = viewModelScope.launch {
        logD("confirmUpdate() called")
        if (!isFormValid.value) {
            logD("confirmUpdate() aborted: form not valid")
            return@launch
        }

        val s = _uiState.value
        if (s.isSaving) {
            logD("confirmUpdate() aborted: already saving")
            return@launch
        }

        val currentUserId = userId.value
        if (currentUserId == -1L) {
            logD("confirmUpdate() aborted: user not logged in")
            _uiState.update { it.copy(error = "User not logged in.") }
            return@launch
        }

        // Dismiss the dialog and set isSaving to true
        _uiState.update { it.copy(showUpdateConfirmation = false, isSaving = true) }
        logD("confirmUpdate() fetching original FoodLog for id=$logId, userId=$currentUserId")

        try {
            foodRepository.getById(logId, currentUserId).firstOrNull()?.let { originalLog ->
                logD("confirmUpdate() original FoodLog found, updating...")
                val updatedLog = FoodLog(
                    id = logId,
                    name = s.foodName.trim(),
                    calories = s.calories.toDoubleOrNull(),
                    mealType = s.mealType.trim(),
                    notes = s.description.trim().takeIf { it.isNotEmpty() } ?: "",
                    timestamp = originalLog.timestamp,
                    userId = currentUserId
                )
                foodRepository.update(updatedLog)
                logD("confirmUpdate() update() success for id=$logId")
            } ?: run {
                logD("confirmUpdate() original FoodLog not found for id=$logId")
                _uiState.update { it.copy(error = "Log not found.") }
            }

            _uiState.update { it.copy(isSaving = false, saveCompleted = true) }
        } catch (e: Exception) {
            logE("confirmUpdate() update() error for id=$logId", e)
            _uiState.update { it.copy(isSaving = false, error = "Failed to update log.") }
        }
    }

    fun onSaveCompleted() {
        logD("onSaveCompleted() called, resetting saveCompleted=false")
        _uiState.update { it.copy(saveCompleted = false) }
    }
}
