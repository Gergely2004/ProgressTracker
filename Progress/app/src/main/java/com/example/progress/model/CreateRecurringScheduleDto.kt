package com.example.progress.model

import com.google.gson.annotations.SerializedName

data class CreateRecurringScheduleDto(
    @SerializedName("habitId")
    val habitId: Long,

    @SerializedName("start_time")
    val startTime: String,

    @SerializedName("end_time")
    val endTime: String? = null,

    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,

    @SerializedName("repeatPattern")
    val repeatPattern: String = "none",

    @SerializedName("repeatDays")
    val repeatDays: Int = 30,

    @SerializedName("is_custom")
    val isCustom: Boolean = true,

    @SerializedName("participantIds")
    val participantIds: List<Long>? = null,

    @SerializedName("notes")
    val notes: String? = null
)

