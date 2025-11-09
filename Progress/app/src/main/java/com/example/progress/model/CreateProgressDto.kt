package com.example.progress.model

import com.google.gson.annotations.SerializedName

data class CreateProgressDto(
    @SerializedName("scheduleId")
    val scheduleId: Long,
    @SerializedName("date")
    val date: String, // Expecting ISO date or date-time per backend definition
    @SerializedName("logged_time")
    val loggedTime: Double, // Assuming number can be fractional
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("is_completed")
    val isCompleted: Boolean
)

