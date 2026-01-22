package com.habittracker.ml.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class HabitWithCheckIns(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val checkIns: List<CheckIn>
)