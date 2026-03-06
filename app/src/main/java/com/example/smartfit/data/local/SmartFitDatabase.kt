package com.example.smartfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.DateConverter
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.model.TipMessage
import com.example.smartfit.data.model.TipThread
import com.example.smartfit.data.model.User


/**
 * The Room database for SmartFit.
 */
@Database(
    entities = [
        ActivityLog::class,
        User::class,
        FoodLog::class,
        TipThread::class,
        TipMessage::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class SmartFitDatabase : RoomDatabase() {

    abstract fun activityDao(): ActivityDao
    abstract fun userDao(): UserDao
    abstract fun foodLogDao(): FoodLogDao


    abstract fun tipsDao(): TipsDao

    companion object {
        @Volatile
        private var INSTANCE: SmartFitDatabase? = null

        fun getDatabase(context: Context): SmartFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartFitDatabase::class.java,
                    "smartfit.db"
                )
                    // Still using destructive migration for simplicity
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
