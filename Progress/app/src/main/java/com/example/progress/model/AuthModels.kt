package com.example.progress.model

data class User(
    val id: String,
    val email: String,
    val name: String
)
data class AuthRequest(
    val email: String,
    val password: String,
    val name: String? = null // only used in signup
)
data class Tokens(
    val accessToken: String,
    val refreshToken: String
)
data class AuthResponse(
    val tokens: Tokens,
    val user: User
)
