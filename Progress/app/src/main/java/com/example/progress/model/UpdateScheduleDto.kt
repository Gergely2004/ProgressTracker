package com.example.progress.model

import com.google.gson.annotations.SerializedName

data class UpdateScheduleDto(
    @SerializedName("start_time") val startTime: String? = null,
    @SerializedName("end_time") val endTime: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("is_custom") val isCustom: Boolean? = null,
    @SerializedName("participantIds") val participantIds: List<Long>? = null,
    @SerializedName("notes") val notes: String? = null
)

