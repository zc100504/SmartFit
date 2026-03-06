package com.example.smartfit.data.repository

import android.util.Log
import com.example.smartfit.BuildConfig
import com.example.smartfit.data.local.ActivityDao
import com.example.smartfit.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow

class ActivityRepository(private val dao: ActivityDao) {

    // region Logging helpers

    private val TAG = "ActivityRepository"

    private fun logD(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    // endregion

    /**
     * Retrieves all activity logs for a specific user.
     * --- Renamed for better consistency ---
     */
    fun getAllByUser(userId: Long): Flow<List<ActivityLog>> {
        logD("getAllByUser() userId=$userId")
        return dao.getAll(userId)
    }

    /**
     * Retrieves a single activity log by its ID for a specific user.
     */
    fun getById(id: Long, userId: Long): Flow<ActivityLog?> {
        logD("getById() id=$id, userId=$userId")
        return dao.getById(id, userId)
    }

    /**
     * Inserts an activity log. The entity already contains the userId.
     */
    suspend fun insert(log: ActivityLog) {
        logD("insert() log=${log.title} duration=${log.durationMin} userId=${log.userId}")
        dao.insert(log)
        logD("insert() finished")
    }

    /**
     * Updates an activity log.
     */
    suspend fun update(log: ActivityLog) {
        logD("update() id=${log.id} title=${log.title}")
        dao.update(log)
        logD("update() finished for id=${log.id}")
    }

    /**
     * Deletes an activity log.
     */
    suspend fun delete(log: ActivityLog) {
        logD("delete() id=${log.id} title=${log.title}")
        dao.delete(log)
        logD("delete() finished for id=${log.id}")
    }
}
