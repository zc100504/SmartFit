// ui/auth/SignUpViewModel.kt
package com.example.smartfit.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.model.User // 1. Import the User model
import com.example.smartfit.data.repository.PrefsRepository
import com.example.smartfit.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest // 2. Import for password hashing

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

sealed interface SignUpEffect { data object NavigateLogin : SignUpEffect }

class SignUpViewModel(
    private val users: UserRepository,
    private val prefs: PrefsRepository
) : ViewModel() {

    // region Logging helpers

    private val TAG = "SignUpViewModel"

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

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state

    private val _effect = Channel<SignUpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onName(v: String) {
        logD("onName() called, length=${v.length}")
        _state.value = _state.value.copy(name = v, error = null)
    }

    fun onEmail(v: String) {
        logD("onEmail() called, length=${v.length}")
        _state.value = _state.value.copy(email = v, error = null)
    }

    fun onPassword(v: String) {
        logD("onPassword() called, length=${v.length}")
        _state.value = _state.value.copy(password = v, error = null)
    }

    fun onConfirm(v: String) {
        logD("onConfirm() called, length=${v.length}")
        _state.value = _state.value.copy(confirm = v, error = null)
    }

    fun signUp() = viewModelScope.launch {
        val s = _state.value
        logD("signUp() called, name='${s.name}', email='${s.email}', loading=${s.loading}")

        if (s.loading) {
            logD("signUp() aborted: already loading")
            return@launch
        }

        if (s.name.isBlank() || s.email.isBlank() || s.password.length < 6) {
            logD("signUp() validation failed: missing fields or password too short")
            _state.value = s.copy(error = "Please fill name, valid email and 6+ char password.")
            return@launch
        }

        if (s.password != s.confirm) {
            logD("signUp() validation failed: passwords do not match")
            _state.value = s.copy(error = "Passwords do not match.")
            return@launch
        }

        _state.value = s.copy(loading = true, error = null)

        try {
            // 3. Create a User object and hash the password
            val passwordHash = s.password.sha256()
            logD("signUp() password hashed (sha256)")

            val newUser = User(
                username = s.name,
                email = s.email,
                passwordHash = passwordHash
            )
            logD("signUp() calling users.registerUser() for username='${s.name}'")

            // 4. Pass the User object to the repository
            val newUserId = users.registerUser(newUser)
            logD("signUp() registerUser() returned id=$newUserId")

            if (newUserId != -1L) { // Check for a valid ID (not -1, which indicates failure)
                logD("signUp() success, newUserId=$newUserId. Setting loggedIn=false and navigating to Login.")
                prefs.setLoggedIn(false) // after sign-up, send to Login (or set true to go Home)
                _effect.send(SignUpEffect.NavigateLogin)
                logD("signUp() NavigateLogin effect sent")
            } else {
                logD("signUp() failed: username/email may already exist")
                _state.value = _state.value.copy(
                    error = "Sign up failed. A user with this name may already exist."
                )
            }
        } catch (e: Exception) {
            logE("signUp() unexpected error", e)
            _state.value = _state.value.copy(
                error = "Sign up failed due to an unexpected error. Please try again."
            )
        } finally {
            _state.value = _state.value.copy(loading = false)
            logD("signUp() finished, loading=false")
        }
    }
}

// 5. Add a helper function to hash the password
private fun String.sha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}
