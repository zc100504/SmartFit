package com.example.smartfit.data.repository

import android.util.Log
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.local.UserDao // Corrected DAO import
import com.example.smartfit.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * The concrete implementation of the UserRepository.
 * It uses a local data source (UserDao) to perform operations.
 */
class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    // region Logging helpers

    private val TAG = "UserRepository"

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

    override suspend fun registerUser(user: User): Long {
        logD("registerUser() username=${user.username}, email=${user.email}")
        return try {
            val existingUser = userDao.getUserByUsername(user.username)
            if (existingUser == null) {
                val id = userDao.insert(user)
                logD("registerUser() success, newId=$id")
                id
            } else {
                logD("registerUser() failed: username already exists")
                -1L
            }
        } catch (e: Exception) {
            logE("registerUser() error", e)
            -1L
        }
    }

    override suspend fun login(email: String, passwordHash: String): User? {
        logD("login() email=$email")
        return try {
            val user = userDao.getUserByEmail(email)
            val success = user?.passwordHash == passwordHash
            if (success) {
                logD("login() success userId=${user!!.id}")
                user
            } else {
                logD("login() failed: invalid credentials")
                null
            }
        } catch (e: Exception) {
            logE("login() error", e)
            null
        }
    }

    override fun getUserById(id: Long): Flow<User?> {
        logD("getUserById() id=$id")
        return userDao.getUserById(id)
    }

    override suspend fun getUserByUsername(username: String): User? {
        logD("getUserByUsername() username=$username")
        return try {
            val user = userDao.getUserByUsername(username)
            logD("getUserByUsername() result isNull=${user == null}")
            user
        } catch (e: Exception) {
            logE("getUserByUsername() error", e)
            null
        }
    }

    override suspend fun updateUser(user: User) {
        logD("updateUser() id=${user.id}, username=${user.username}")
        try {
            userDao.update(user)
            logD("updateUser() finished for id=${user.id}")
        } catch (e: Exception) {
            logE("updateUser() error for id=${user.id}", e)
        }
    }

    override suspend fun updatePassword(userId: Long, newPasswordHash: String) {
        logD("updatePassword() userId=$userId")
        try {
            userDao.updatePassword(userId, newPasswordHash)
            logD("updatePassword() finished for userId=$userId")
        } catch (e: Exception) {
            logE("updatePassword() error for userId=$userId", e)
        }
    }
}
