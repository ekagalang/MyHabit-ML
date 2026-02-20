package com.habittracker.ml.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.preferences.AuthPreferences
import com.habittracker.ml.data.remote.ApiClient
import com.habittracker.ml.data.repository.SyncRepository
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "SyncWorker"
        private const val UNIQUE_WORK_NAME = "habit_sync_periodic"
        private const val ONE_TIME_WORK_NAME = "habit_sync_manual"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Periodic sync scheduled")
        }

        fun triggerManualSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
            Log.d(TAG, "Manual sync triggered")
        }

        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "Periodic sync cancelled")
        }
    }

    override suspend fun doWork(): Result {
        val authPreferences = AuthPreferences(applicationContext)

        if (!authPreferences.isLoggedIn()) {
            Log.d(TAG, "Not logged in, skipping sync")
            return Result.success()
        }

        return try {
            val apiService = ApiClient.getService(applicationContext)
            val database = HabitDatabase.getDatabase(applicationContext)

            val syncRepository = SyncRepository(
                apiService = apiService,
                habitDao = database.habitDao(),
                checkInDao = database.checkInDao(),
                authPreferences = authPreferences
            )

            val result = syncRepository.fullSync()

            if (result.success) {
                Log.d(TAG, "Sync successful: ${result.habitsUploaded} habits up, ${result.habitsDownloaded} down, ${result.checkInsUploaded} checkins up, ${result.checkInsDownloaded} down")
                Result.success()
            } else {
                Log.w(TAG, "Sync failed: ${result.error}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            Result.retry()
        }
    }
}
