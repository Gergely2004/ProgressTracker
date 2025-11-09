package com.example.progress.model

import com.google.gson.annotations.SerializedName

data class CreateHabitDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    // Backend expects camelCase for categoryId as per API contract
    @SerializedName("categoryId")
    val categoryId: Long,

    @SerializedName("goal")
    val goal: String
)
