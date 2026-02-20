package com.habittracker.ml.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.entities.HabitTemplate
import com.habittracker.ml.data.local.entities.Prediction
import com.habittracker.ml.data.templates.DefaultTemplates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Habit::class, CheckIn::class, Prediction::class, HabitTemplate::class],
    version = 6,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao
    abstract fun predictionDao(): PredictionDao
    abstract fun habitTemplateDao(): HabitTemplateDao

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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .addCallback(HabitDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class HabitDatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.habitTemplateDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(habitTemplateDao: HabitTemplateDao) {
            // Check if templates exist, if not seed default ones
            val existingTemplates = habitTemplateDao.getTemplateById(1)
            if (existingTemplates == null) {
                DefaultTemplates.allTemplates.forEach { template ->
                    // Set ID to 0 to let AutoIncrement handle it, or keep it if we want fixed IDs
                    // Since we are seeding, we can just insert them.
                    // However, DefaultTemplates have IDs 1-30. If we insert them with IDs, 
                    // and then user adds one, auto-increment will start from 31+.
                    habitTemplateDao.insertTemplate(template)
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN repeatDays TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `habit_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `icon` TEXT NOT NULL,
                        `defaultFrequency` TEXT NOT NULL DEFAULT 'daily',
                        `defaultReminderTime` TEXT,
                        `tags` TEXT NOT NULL DEFAULT ''
                    )
                """)
            }
        }

        private val MIGRATION_4_5 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Change mood from INT to TEXT
                database.execSQL("ALTER TABLE check_ins ADD COLUMN mood_new TEXT")
                database.execSQL("UPDATE check_ins SET mood_new = CASE mood WHEN 5 THEN 'very_happy' WHEN 4 THEN 'happy' WHEN 3 THEN 'neutral' WHEN 2 THEN 'sad' WHEN 1 THEN 'very_sad' ELSE NULL END")

                // Add new fields
                database.execSQL("ALTER TABLE check_ins ADD COLUMN isLate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE check_ins ADD COLUMN minutesLate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE check_ins ADD COLUMN energyLevel INTEGER")
                database.execSQL("ALTER TABLE check_ins ADD COLUMN stressLevel INTEGER")
                database.execSQL("ALTER TABLE check_ins ADD COLUMN location TEXT")
                database.execSQL("ALTER TABLE check_ins ADD COLUMN weather TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add server sync columns to habits
                database.execSQL("ALTER TABLE habits ADD COLUMN serverId INTEGER")
                database.execSQL("ALTER TABLE habits ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")

                // Add server sync columns to check_ins
                database.execSQL("ALTER TABLE check_ins ADD COLUMN serverId INTEGER")
                database.execSQL("ALTER TABLE check_ins ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
