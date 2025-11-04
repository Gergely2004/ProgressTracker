package com.example.progress.repository

import android.content.Context
import com.example.progress.model.AuthRequest
import com.example.progress.network.RetrofitClient

class AuthRepository(context: Context) {

    private val api = RetrofitClient.getInstance(context)
    suspend fun login(email: String, password: String) =
        api.login(AuthRequest(email = email, password = password))
    suspend fun signup(username: String, email: String, password: String) =
        api.signup(AuthRequest(email = email, password = password, username = username))

}