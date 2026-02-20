package com.habittracker.ml.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HabitDto(
    val id: Long = 0,
    @SerializedName("user_id")
    val userId: Long = 0,
    val name: String,
    val description: String = "",
    val category: String,
    @SerializedName("target_frequency")
    val targetFrequency: Int,
    @SerializedName("reminder_time")
    val reminderTime: String? = null,
    @SerializedName("reminder_enabled")
    val reminderEnabled: Boolean = false,
    @SerializedName("repeat_days")
    val repeatDays: String? = null,
    val color: String = "#6366F1",
    val icon: String = "🎯",
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("check_ins")
    val checkIns: List<CheckInDto>? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class CheckInDto(
    val id: Long = 0,
    @SerializedName("habit_id")
    val habitId: Long,
    val timestamp: String? = null,
    val date: String,
    @SerializedName("completed_at")
    val completedAt: String,
    val note: String? = null,
    val mood: String? = null,
    @SerializedName("is_late")
    val isLate: Boolean = false,
    @SerializedName("minutes_late")
    val minutesLate: Int = 0,
    @SerializedName("energy_level")
    val energyLevel: Int? = null,
    @SerializedName("stress_level")
    val stressLevel: Int? = null,
    val location: String? = null,
    val weather: String? = null
)

data class HabitCreateRequest(
    val name: String,
    val description: String = "",
    val category: String,
    @SerializedName("target_frequency")
    val targetFrequency: Int,
    @SerializedName("reminder_time")
    val reminderTime: String? = null,
    @SerializedName("reminder_enabled")
    val reminderEnabled: Boolean = false,
    @SerializedName("repeat_days")
    val repeatDays: String? = null,
    val color: String = "#6366F1",
    val icon: String = "🎯"
)

data class CheckInCreateRequest(
    @SerializedName("habit_id")
    val habitId: Long,
    val date: String,
    @SerializedName("completed_at")
    val completedAt: String,
    val note: String? = null,
    val mood: String? = null,
    @SerializedName("is_late")
    val isLate: Boolean = false,
    @SerializedName("minutes_late")
    val minutesLate: Int = 0,
    @SerializedName("energy_level")
    val energyLevel: Int? = null,
    @SerializedName("stress_level")
    val stressLevel: Int? = null,
    val location: String? = null,
    val weather: String? = null
)

data class BulkCheckInRequest(
    @SerializedName("check_ins")
    val checkIns: List<CheckInCreateRequest>
)

data class SyncRequest(
    val habits: List<HabitDto>,
    @SerializedName("check_ins")
    val checkIns: List<CheckInDto>
)

data class SyncResponse(
    val success: Boolean,
    @SerializedName("habits_created")
    val habitsCreated: Int = 0,
    @SerializedName("habits_updated")
    val habitsUpdated: Int = 0,
    @SerializedName("check_ins_created")
    val checkInsCreated: Int = 0,
    val habits: List<HabitDto> = emptyList(),
    @SerializedName("check_ins")
    val checkIns: List<CheckInDto> = emptyList()
)

data class ExportData(
    @SerializedName("exportDate")
    val exportDate: Long,
    val version: String,
    val habits: List<HabitDto>,
    @SerializedName("checkIns")
    val checkIns: List<CheckInDto>
)
