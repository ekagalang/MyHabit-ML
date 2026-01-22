package com.habittracker.ml.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit

@Database(
    entities = [Habit::class, CheckIn::class],
    version = 1,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}