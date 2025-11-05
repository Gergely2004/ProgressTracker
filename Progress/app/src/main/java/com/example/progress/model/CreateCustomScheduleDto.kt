package com.example.progress.model

import com.google.gson.annotations.SerializedName

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
    val isCustom: Boolean = true,

    @SerializedName("participantIds")
    val participantIds: List<Long>? = null,

    @SerializedName("notes")
    val notes: String? = null
)
