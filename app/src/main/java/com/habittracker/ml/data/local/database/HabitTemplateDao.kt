package com.habittracker.ml.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.habittracker.ml.data.local.entities.HabitTemplate

@Dao
interface HabitTemplateDao {
    @Query("SELECT * FROM habit_templates")
    fun getAllTemplates(): LiveData<List<HabitTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: HabitTemplate)

    @Update
    suspend fun updateTemplate(template: HabitTemplate)

    @Delete
    suspend fun deleteTemplate(template: HabitTemplate)
    
    @Query("DELETE FROM habit_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Int)

    @Query("SELECT * FROM habit_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Int): HabitTemplate?
}
