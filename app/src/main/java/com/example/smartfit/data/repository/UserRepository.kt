package com.example.smartfit.data.repository

import com.example.smartfit.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the user repository.
 * It defines the contract for data operations related to users.
 */
interface UserRepository {

    /**
     * Registers a new user. Returns the ID of the new user, or -1 if registration fails (e.g., user exists).
     */
    suspend fun registerUser(user: User): Long

    /**
     * Attempts to log in a user with a given email and password hash.
     * Returns the User object on success, or null on failure.
     */
    suspend fun login(email: String, passwordHash: String): User?

    /**
     * Retrieves a user by their unique ID as a reactive Flow.
     */
    fun getUserById(id: Long): Flow<User?>

    /**
     * Retrieves a user by their username for validation.
     */
    suspend fun getUserByUsername(username: String): User?

    suspend fun updateUser(user: User)

    suspend fun updatePassword(userId: Long, newPasswordHash: String)
}
