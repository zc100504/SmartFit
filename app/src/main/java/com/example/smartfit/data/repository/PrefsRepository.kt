package com.example.smartfit.data.repository

import android.content.Context
import android.util.Log
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow

class PrefsRepository(private val appContext: Context) {

    // region Logging helpers

    private val TAG = "PrefsRepository"

    private fun logD(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    // endregion

    // Onboarding
    /*
    fun isOnboarded(): Flow<Boolean> = UserPreferences.getOnboarded(appContext)
    suspend fun setOnboarded(v: Boolean) = UserPreferences.setOnboarded(appContext, v)
    */

    // Auth
    fun isLoggedIn(): Flow<Boolean> {
        logD("isLoggedIn() called")
        return UserPreferences.getLoggedIn(appContext)
    }

    suspend fun setLoggedIn(v: Boolean) {
        logD("setLoggedIn() v=$v")
        UserPreferences.setLoggedIn(appContext, v)
        logD("setLoggedIn() finished")
    }

    // --- FIX: Add methods for getting and setting the current user ID ---

    /**
     * Retrieves the Flow of the currently logged-in user's ID.
     * Emits -1L if no user is logged in or the ID is not set.
     */
    fun getUserId(): Flow<Long> {
        logD("getUserId() called")
        return UserPreferences.getUserId(appContext)
    }

    /**
     * Stores the ID of the currently logged-in user.
     */
    suspend fun setUserId(id: Long) {
        logD("setUserId() id=$id")
        UserPreferences.setUserId(appContext, id)
        logD("setUserId() finished")
    }

    // Theme: "SYSTEM" | "LIGHT" | "DARK"
    fun themeMode(): Flow<String> {
        logD("themeMode() called")
        return UserPreferences.getTheme(appContext)
    }

    suspend fun setThemeMode(mode: String) {
        logD("setThemeMode() mode=$mode")
        UserPreferences.setTheme(appContext, mode)
        logD("setThemeMode() finished")
    }

    // Goals
    /*
    fun stepGoal(): Flow<Int> = UserPreferences.getStepGoal(appContext)
    suspend fun setStepGoal(goal: Int) = UserPreferences.setStepGoal(appContext, goal)
    */
}
