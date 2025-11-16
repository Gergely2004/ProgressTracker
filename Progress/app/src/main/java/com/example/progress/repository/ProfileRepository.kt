package com.example.progress.repository

import android.content.Context
import com.example.progress.model.ProfileResponseDto
import com.example.progress.network.RetrofitClient

class ProfileRepository(context: Context) {
    private val api by lazy { RetrofitClient.getInstance(context)}

    suspend fun getProfile(): ProfileResponseDto = api.getProfile()
}