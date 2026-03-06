package com.example.smartfit.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A sealed interface to represent any item that can appear on the log timeline.
 * It ensures all log items have a common structure for the UI.
 */
sealed interface LogItem {
    val id: Long
    val icon: ImageVector
    val calories: Double?
    val type: String

    // --- FIX START ---

    // 1. The primary timestamp should be a Long for efficient sorting and filtering.
    val timestamp: Long

    // 2. The displayDate is now a computed property derived from the timestamp.
    //    This removes the need to pass it separately and ensures consistency.
    val displayDate: String
        get() {
            // Create a Date object only when needed for formatting.
            val date = Date(timestamp)
            // Using a simple, readable format for the UI.
            val format = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            return format.format(date)
        }

    // --- FIX END ---
}
