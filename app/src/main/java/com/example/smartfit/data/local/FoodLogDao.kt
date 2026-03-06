package com.example.smartfit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartfit.data.model.FoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {

    /**
     * Inserts a food log into the table. If the log already exists, it replaces it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: FoodLog)

    /**
     * Updates an existing food log in the table.
     */
    @Update
    suspend fun update(log: FoodLog)

    /**
     * Deletes a food log from the table.
     */
    @Delete
    suspend fun delete(log: FoodLog)

    /**
     * Retrieves a single food log by its ID for a specific user.
     */
    // --- FIX 1: Add userId to filter the query and correct table name ---
    @Query("SELECT * FROM food_log WHERE id = :id AND userId = :userId")
    fun getById(id: Long, userId: Long): Flow<FoodLog?>

    /**
     * Retrieves all food logs for a specific user, ordered by the most recent.
     */
    // --- FIX 2: Add userId to filter the query and correct table name ---
    @Query("SELECT * FROM food_log WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAll(userId: Long): Flow<List<FoodLog>>

    /**
     * Deletes all food logs for a specific user. Useful for logout.
     */
    // --- FIX 3 (Recommended): Add a function to clear data on logout ---
    @Query("DELETE FROM food_log WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)

    @Query("""
        SELECT * FROM food_log 
        WHERE userId = :userId 
          AND timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
    """)
    suspend fun getInRange(
        userId: Long,
        from: Long,
        to: Long
    ): List<FoodLog>
}
