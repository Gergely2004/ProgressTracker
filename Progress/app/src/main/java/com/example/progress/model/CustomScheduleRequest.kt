package com.example.progress.model

import com.google.gson.annotations.SerializedName

// Request for POST /schedule/custom
// Assumptions based on provided API description
// - 'date' likely in YYYY-MM-DD (server may accept date-time; using date-only aligns with GET /schedule/day)
// - 'start_time' ISO 8601 date-time (e.g., 2025-11-04T08:30:00)
// - 'is_custom' defaults to true
// - Optional fields as described

data class CustomScheduleRequest(
    @SerializedName("habitId")
    val habitId: Long,

    @SerializedName("date")
    val date: String,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("is_custom")
    val isCustom: Boolean = true,

    @SerializedName("end_time")
    val endTime: String? = null,

    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,

    @SerializedName("participantIds")
    val participantIds: List<Long>? = null,

    @SerializedName("notes")
    val notes: String? = null
)

