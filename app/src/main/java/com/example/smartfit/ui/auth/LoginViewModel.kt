// ui/auth/LoginViewModel.kt
package com.example.smartfit.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.repository.PrefsRepository
import com.example.smartfit.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest // 1. Import for password hashing

data class LoginUiState(
    val email: String = "", // Note: The UI says email, but our logic uses username. Let's stick to username for logic.
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

sealed interface LoginEffect { data object NavigateHome : LoginEffect }

class LoginViewModel(
    private val users: UserRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    // region Logging helpers

    private val TAG = "LoginViewModel"

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

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEmail(v: String) {
        logD("onEmail() called, length=${v.length}")
        _state.value = _state.value.copy(email = v, error = null)
    }

    fun onPassword(v: String) {
        logD("onPassword() called, length=${v.length}")
        _state.value = _state.value.copy(password = v, error = null)
    }

    fun login() = viewModelScope.launch {
        val s = _state.value
        logD("login() called, email='${s.email}', loading=${s.loading}")

        if (s.loading) {
            logD("login() aborted: already loading")
            return@launch
        }
        if (s.email.isBlank() || s.password.isBlank()) {
            logD("login() validation failed: empty email or password")
            _state.value = s.copy(error = "Please enter username and password.")
            return@launch
        }
        _state.value = s.copy(loading = true, error = null)

        // 2. Hash the password from the login form
        val passwordHash = s.password.sha256()
        logD("login() password hashed (sha256)")

        try {
            // 3. Use the username and the *hashed* password to log in
            logD("login() calling users.login(email='${s.email}')")
            val loggedInUser = users.login(s.email, passwordHash)

            if (loggedInUser != null) {
                logD("login() success, userId=${loggedInUser.id}")

                prefs.setLoggedIn(true)              // Set the logged-in flag
                logD("login() prefs.setLoggedIn(true) saved")

                prefs.setUserId(loggedInUser.id)     // Save the user's ID
                logD("login() prefs.setUserId(${loggedInUser.id}) saved")

                _effect.send(LoginEffect.NavigateHome)
                logD("login() NavigateHome effect sent")
            } else {
                logD("login() failed: invalid credentials")
                _state.value = _state.value.copy(error = "Invalid email or password")
            }
        } catch (e: Exception) {
            logE("login() unexpected error", e)
            _state.value = _state.value.copy(error = "Login failed, please try again.")
        } finally {
            _state.value = _state.value.copy(loading = false)
            logD("login() finished, loading=false")
        }
    }
}

// 4. Add the same helper function to hash the password
private fun String.sha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}
