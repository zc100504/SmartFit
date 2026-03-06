package com.example.smartfit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    // It's critical to store a hashed password, not the plain text one.
    val passwordHash: String,
    val avatarUrl: String = "https://i.pravatar.cc/150?img=12"
)
