package com.habittracker.ml.data.repository

import androidx.lifecycle.LiveData
import com.habittracker.ml.data.local.database.PredictionDao
import com.habittracker.ml.data.local.entities.Prediction

class PredictionRepository(private val predictionDao: PredictionDao) {

    fun getLatestPrediction(): LiveData<Prediction?> {
        return predictionDao.getLatestPrediction()
    }

    suspend fun getLatestPredictionByType(type: String): Prediction? {
        return predictionDao.getLatestPredictionByType(type)
    }

    fun getAllPredictions(): LiveData<List<Prediction>> {
        return predictionDao.getAllPredictions()
    }

    suspend fun insertPrediction(prediction: Prediction): Long {
        return predictionDao.insertPrediction(prediction)
    }

    suspend fun deleteExpiredPredictions() {
        val currentTime = System.currentTimeMillis()
        predictionDao.deleteExpiredPredictions(currentTime)
    }

    suspend fun deleteAllPredictions() {
        predictionDao.deleteAllPredictions()
    }
}