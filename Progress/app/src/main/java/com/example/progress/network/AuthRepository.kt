package com.example.progress.network

import android.content.Context
import com.example.progress.model.AuthRequest


class AuthRepository(context: Context) {

    private val api = RetrofitClient.getInstance(context)
    suspend fun login(email: String, password: String) =
        api.login(AuthRequest(email = email, password = password))

}
