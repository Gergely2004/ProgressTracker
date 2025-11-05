package com.example.progress.model

import com.google.gson.annotations.SerializedName



data class CreateScheduleDto(
    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("habit_id")
    val habitId: Long? = null,

    @SerializedName("repeat_pattern")
    val repeatPattern: String = "none",

    @SerializedName("duration_minutes")
    val durationMinutes: Int,

    @SerializedName("new_habit")
    val newHabit: CreateHabitDto? = null
)

