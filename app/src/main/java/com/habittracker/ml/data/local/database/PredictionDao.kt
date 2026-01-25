package com.habittracker.ml.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.habittracker.ml.data.local.entities.Prediction

@Dao
interface PredictionDao {

    @Query("SELECT * FROM predictions ORDER BY generatedAt DESC LIMIT 1")
    fun getLatestPrediction(): LiveData<Prediction?>

    @Query("SELECT * FROM predictions WHERE predictionType = :type ORDER BY generatedAt DESC LIMIT 1")
    suspend fun getLatestPredictionByType(type: String): Prediction?

    @Query("SELECT * FROM predictions ORDER BY generatedAt DESC")
    fun getAllPredictions(): LiveData<List<Prediction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: Prediction): Long

    @Query("DELETE FROM predictions WHERE validUntil < :currentTime")
    suspend fun deleteExpiredPredictions(currentTime: Long)

    @Query("DELETE FROM predictions")
    suspend fun deleteAllPredictions()
}