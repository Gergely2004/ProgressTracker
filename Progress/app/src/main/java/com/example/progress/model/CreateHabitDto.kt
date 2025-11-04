package com.example.progress.model

import com.google.gson.annotations.SerializedName

// Input model for creating a habit
data class CreateHabitDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    // The backend expects `categoryId` (camelCase). Previously this was serialized as `category_id` which caused 400.
    @SerializedName("categoryId")
    val categoryId: Long,

    @SerializedName("goal")
    val goal: String
)
