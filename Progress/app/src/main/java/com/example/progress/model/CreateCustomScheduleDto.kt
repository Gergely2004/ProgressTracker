package com.example.progress.model

import com.google.gson.annotations.SerializedName

// Matches: { habitId, date, start_time, end_time?, duration_minutes?, is_custom }
// All date/time fields are strings (ISO 8601).

data class CreateCustomScheduleDto(
    @SerializedName("habitId")
    val habitId: Long,

    @SerializedName("date")
    val date: String,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("end_time")
    val endTime: String? = null,

    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,

    @SerializedName("is_custom")
    val isCustom: Boolean = true
)

