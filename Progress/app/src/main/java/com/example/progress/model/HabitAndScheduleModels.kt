package com.example.progress.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ScheduleResponseDto(
    val id: Long,
    @SerializedName("start_time")
    val startTime: LocalDateTime? = null,
    @SerializedName("end_time")
    val endTime: LocalDateTime? = null,
    val status: String? = null,
    val date: String? = null,
    @SerializedName("is_custom")
    val isCustom: Boolean,
    @SerializedName("created_at")
    val createdAt: LocalDateTime? = null,
    @SerializedName("updated_at")
    val updatedAt: LocalDateTime? = null,
    val type: String? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    val notes: String? = null,
    val participants: List<ParticipantDto>? = emptyList(),
    val habit: HabitResponseDto? = null,
    val progress: List<ProgressResponseDto>? = emptyList(),
    val isParticipantOnly: Boolean,
)

data class HabitResponseDto (
    val id: Long,
    val name: String,
    val description: String? = null,
    val category: HabitCategory,
    val goal: String,
    @SerializedName("created_at")
    val createdAt: LocalDateTime,
    @SerializedName("updated_at")
    val updatedAt: LocalDateTime
)

data class HabitCategory (
    val id: Long,
    val name: String,
    @SerializedName("iconUrl")
    val iconUrl: String? = null
)

data class ProgressResponseDto(
    val id: Long,
    @SerializedName("scheduleId")
    val scheduleId: Long,
    @SerializedName("date")
    val date: String,
    @SerializedName("logged_time")
    val loggedTime: Double,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("is_completed")
    val isCompleted: Boolean,
    @SerializedName("created_at")
    val createdAt: LocalDateTime? = null,
    @SerializedName("updated_at")
    val updatedAt: LocalDateTime? = null
)
data class ParticipantDto(
    val id: Long,
    val name: String,
    val email: String,
    val profileImage: String? = null
)
