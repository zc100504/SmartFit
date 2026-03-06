package com.example.smartfit.data.local // Changed from com.example.smartfit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartfit.data.model.User // Import the model from its new location
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPasswordHash: String)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): Flow<User?>
}
