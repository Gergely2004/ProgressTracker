package com.example.progress.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val email: String,
    val name: String
)
data class AuthRequest(
    val email: String,
    val password: String,
    val username: String? = null // only used in signup
)
data class Tokens(
    val accessToken: String,
    val refreshToken: String
)
data class AuthResponseDto(
    @SerializedName("message")
    val message: String,
    @SerializedName ("tokens")
    val tokens: Tokens,
    @SerializedName("user")
    val user: User,
)
