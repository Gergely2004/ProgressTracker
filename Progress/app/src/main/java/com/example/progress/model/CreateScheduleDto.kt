package com.example.progress.model

import com.google.gson.annotations.SerializedName

// Request body for creating a schedule
// Assumptions based on ScheduleResponse fields and feature description
// - start_time: ISO 8601 string (server parses to LocalDateTime)
// - habitId: either an existing habit ID or omitted if creating a new habit inline
// - repeat_pattern: daily | weekdays | weekends | none
// - duration_minutes: goal duration in minutes for the scheduled session
// - optional: new_habit fields when creating a habit inline

data class CreateScheduleDto(
    @SerializedName("start_time")
    val startTime: String,

    // Either provide habitId to link an existing habit, or provide newHabit to create inline
    @SerializedName("habit_id")
    val habitId: Long? = null,

    @SerializedName("repeat_pattern")
    val repeatPattern: String = "none",

    @SerializedName("duration_minutes")
    val durationMinutes: Int,

    // Inline habit creation when habitId is null
    @SerializedName("new_habit")
    val newHabit: CreateHabitDto? = null
)

