package com.example.smartfit.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.model.User // 1. Add User model import
import com.example.smartfit.data.repository.PrefsRepository
import com.example.smartfit.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val prefs: PrefsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    // region Logging helpers

    private val TAG = "ProfileViewModel"

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

    // ---------------------------------------------------------
    // 1. USER STREAM
    // ---------------------------------------------------------
    private val userId: StateFlow<Long> =
        prefs.getUserId()
            .onEach { id -> logD("userId flow emitted: $id") }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), -1L)

    // Make this public so the Edit screen can observe the full user object
    val user: StateFlow<User?> = userId
        .flatMapLatest { id ->
            if (id == -1L) {
                logD("user flow: invalid userId (-1), emitting null")
                flowOf(null)
            } else {
                logD("user flow: loading user for id=$id")
                userRepository.getUserById(id)
            }
        }
        .onEach { u ->
            logD("user flow emitted: ${u?.id ?: "null"}")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val username: StateFlow<String> = user
        .map { it?.username ?: "Guest" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Guest")

    val email: StateFlow<String> = user
        .map { it?.email ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // ---------------------------------------------------------
    // 2. THEME SETTINGS
    // ---------------------------------------------------------
    val themeMode: StateFlow<String> =
        prefs.themeMode()
            .onEach { mode -> logD("themeMode flow emitted: $mode") }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SYSTEM")

    fun setTheme(mode: String) = viewModelScope.launch {
        logD("setTheme() called with mode=$mode")
        try {
            prefs.setThemeMode(mode)
            logD("setTheme() finished")
        } catch (e: Exception) {
            logE("setTheme() error", e)
        }
    }

    // ---------------------------------------------------------
    // 3. EDIT PROFILE
    // ---------------------------------------------------------

    fun updateUserProfile(newName: String, newEmail: String, newAvatarUrl: String) =
        viewModelScope.launch {
            logD("updateUserProfile() called, name='$newName', email='$newEmail'")
            val currentUser = user.value
            if (currentUser != null) {
                val updatedUser = currentUser.copy(
                    username = newName,
                    email = newEmail,
                    avatarUrl = newAvatarUrl // The new field
                )
                try {
                    userRepository.updateUser(updatedUser)
                    logD("updateUserProfile() success for userId=${currentUser.id}")
                } catch (e: Exception) {
                    logE("updateUserProfile() error for userId=${currentUser.id}", e)
                }
            } else {
                logD("updateUserProfile() aborted: currentUser is null")
            }
        }

    fun String.sha256(): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(this.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        logD("changePassword() called")

        val currentUser = user.first { it != null }

        if (currentUser == null) {
            logD("changePassword() aborted: currentUser is null")
            onError("User not found. Please log in again.")
            return@launch
        }

        // --- 2. FIX: Use the SAME SHA-256 hashing for the check ---
        val currentPasswordHash = currentPassword.sha256()
        logD("changePassword() current password hashed")

        if (currentUser.passwordHash != currentPasswordHash) {
            logD("changePassword() failed: current password incorrect")
            onError("The current password you entered is incorrect.")
            return@launch
        }

        // --- 3. FIX: Also hash the NEW password with SHA-256 ---
        val newPasswordHash = newPassword.sha256()
        logD("changePassword() new password hashed, updating repository")

        try {
            userRepository.updatePassword(currentUser.id, newPasswordHash)
            logD("changePassword() success for userId=${currentUser.id}")
            onSuccess() // Signal the UI that the operation was successful
        } catch (e: Exception) {
            logE("changePassword() error for userId=${currentUser.id}", e)
            onError("Failed to change password. Please try again.")
        }
    }

    // ---------------------------------------------------------
    // 4. LOGOUT
    // ---------------------------------------------------------
    fun logout() = viewModelScope.launch {
        logD("logout() called")
        try {
            prefs.setUserId(-1L)
            prefs.setLoggedIn(false)
            logD("logout() finished: userId reset to -1, loggedIn=false")
        } catch (e: Exception) {
            logE("logout() error", e)
        }
    }
}
