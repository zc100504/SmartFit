package com.example.smartfit.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.smartfit.ui.icon.SmartFitIcons

@Entity(
    tableName = "activity_logs",
    // --- FIX 1: Add an index on the new userId column for faster queries ---
    indices = [Index(value = ["userId"])]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,

    // --- FIX 2: Add the userId field to link this log to a specific user ---
    val userId: Long,

    override val timestamp: Long,
    override val type: String,
    val title: String?,
    val durationMin: Int? = null,
    val distance: Double?,
    override val calories: Double? = null,
    val notes: String? = null,


) : LogItem {

    override val displayDate: String
        get() {
            val date = Date(timestamp)
            return SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(date)
        }

    override val icon: ImageVector
        get() = SmartFitIcons.Running
}
