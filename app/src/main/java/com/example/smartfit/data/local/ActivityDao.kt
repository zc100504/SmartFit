package com.example.smartfit.data.local

import androidx.room.*
import com.example.smartfit.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    // --- FIX 1: Add userId parameter to filter all results for a specific user ---
    @Query("SELECT * FROM activity_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAll(userId: Long): Flow<List<ActivityLog>>

    // --- FIX 2: Add userId parameter to ensure a user can only get their own log ---
    @Query("SELECT * FROM activity_logs WHERE id = :id AND userId = :userId")
    fun getById(id: Long, userId: Long): Flow<ActivityLog?>

    // Insert, Update, and Delete operate on a specific entity that will already
    // have the correct userId, so no changes are needed here.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLog): Long

    @Update
    suspend fun update(log: ActivityLog)

    @Delete
    suspend fun delete(log: ActivityLog)

    // --- FIX 3 (Optional but Recommended): Add a function to clear data on logout ---
    @Query("DELETE FROM activity_logs WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)

    @Query("""
        SELECT * FROM activity_logs
        WHERE userId = :userId 
          AND timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
    """)
    suspend fun getInRange(
        userId: Long,
        from: Long,
        to: Long
    ): List<ActivityLog>

    @Query("""
    SELECT COUNT(*) FROM activity_logs
    WHERE userId = :userId AND timestamp >= :startOfWeek
""")
    fun getWeeklyWorkoutCount(userId: Long, startOfWeek: Long): Flow<Int>

    @Query("""
    SELECT SUM(calories) FROM activity_logs
    WHERE userId = :userId AND timestamp >= :startOfWeek
""")
    fun getWeeklyCalories(userId: Long, startOfWeek: Long): Flow<Double?>

}
