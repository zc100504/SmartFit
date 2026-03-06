package com.example.smartfit.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.smartfit.ui.icon.SmartFitIcons

/**
 * Represents a single food log entry in the database.
 * This class defines the 'food_log' table schema and implements LogItem for UI compatibility.
 */

@Entity(
    tableName = "food_log",
    // --- FIX 1: Add an index on the new userId column for faster queries ---
    indices = [Index(value = ["userId"])]
)
data class FoodLog(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,

    // --- FIX 2: Add the userId field to link this log to a specific user ---
    val userId: Long,

    val name: String,
    override val calories: Double?,
    val mealType: String,
    override val timestamp: Long,
    val notes: String
) : LogItem {
    // This part is for the UI, derived from the database fields
    override val displayDate: String
        get() {
            val date = Date(timestamp) // Create a Date object from the Long
            return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
        }
    override val icon: ImageVector
        get() = SmartFitIcons.Food

    // --- FIX 3: Correctly implement the 'type' property ---
    // The 'type' for a FoodLog should be a fixed string, like "Food".
    override val type: String
        get() = "Food"
}
