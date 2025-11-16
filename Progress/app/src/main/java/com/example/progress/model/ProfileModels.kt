package com.example.progress.model

import com.google.gson.annotations.SerializedName

data class ProfileResponseDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("email")
    val email: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,
    @SerializedName("profileImageBase64")
    val profileImageBase64: String?,
    @SerializedName("coverImageUrl")
    val coverImageUrl: String?,
    @SerializedName("fcmToken")
    val fcmToken: String?,
    @SerializedName("preferences")
    val preferences: Map<String, Any>?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

