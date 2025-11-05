package com.example.progress.model

import com.google.gson.annotations.SerializedName

data class CreateHabitDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    // Backend expects snake_case for consistency with other fields
    @SerializedName("category_id")
    val categoryId: Long,

    @SerializedName("goal")
    val goal: String
)
