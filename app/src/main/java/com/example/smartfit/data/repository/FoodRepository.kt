package com.example.smartfit.data.repository

import android.util.Log
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.local.FoodLogDao
import com.example.smartfit.data.model.FoodLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository module for handling data operations for FoodLogs.
 * It abstracts the data source (Room DAO) from the rest of the app.
 */
class FoodRepository(private val dao: FoodLogDao) {

    // region Logging helpers

    private val TAG = "FoodRepository"

    private fun logD(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    // endregion

    /**
     * Retrieves all food logs for a specific user as a Flow.
     * --- FIX 1: Add userId parameter and pass it to the DAO ---
     */
    fun getAllByUser(userId: Long): Flow<List<FoodLog>> {
        logD("getAllByUser() userId=$userId")
        return dao.getAll(userId)
    }

    /**
     * Retrieve a food log from the given data source that matches with the [id] for a specific user.
     * --- FIX 2: Add userId parameter and pass it to the DAO ---
     */
    fun getById(id: Long, userId: Long): Flow<FoodLog?> {
        logD("getById() id=$id, userId=$userId")
        return dao.getById(id, userId)
    }

    /**
     * Insert a food log in the data source.
     * The 'log' object already contains the userId, so no parameter change is needed.
     */
    suspend fun insert(log: FoodLog) {
        logD("insert() name=${log.name} calories=${log.calories} userId=${log.userId}")
        dao.insert(log)
        logD("insert() finished")
    }

    /**
     * Update a food log in the data source.
     */
    suspend fun update(log: FoodLog) {
        logD("update() id=${log.id} name=${log.name} calories=${log.calories}")
        dao.update(log)
        logD("update() finished for id=${log.id}")
    }

    /**
     * Delete a food log from the data source.
     */
    suspend fun delete(log: FoodLog) {
        logD("delete() id=${log.id} name=${log.name}")
        dao.delete(log)
        logD("delete() finished for id=${log.id}")
    }
}
