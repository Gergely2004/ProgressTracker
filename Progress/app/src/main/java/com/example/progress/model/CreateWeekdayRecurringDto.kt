package com.example.progress.model

import com.google.gson.annotations.SerializedName

// DTO for POST /schedule/recurring/weekdays
// daysOfWeek: 1 = Monday ... 7 = Sunday
// numberOfWeeks default is 4
// start_time: full date-time string (ISO 8601). Backend should use time portion.

data class CreateWeekdayRecurringDto(
    @SerializedName("habitId")
    val habitId: Long,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("daysOfWeek")
    val daysOfWeek: List<Int>,
    @SerializedName("numberOfWeeks")
    val numberOfWeeks: Int = 4,
    @SerializedName("participantIds")
    val participantIds: List<Long>? = null,
    @SerializedName("notes")
    val notes: String? = null
)

