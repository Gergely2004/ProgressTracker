package com.example.progress.repository

import android.content.Context
import com.example.progress.model.CreateProgressDto
import com.example.progress.model.ProgressResponseDto
import com.example.progress.network.RetrofitClient
import retrofit2.Response

class ProgressRepository(context: Context) {
    private val api by lazy { RetrofitClient.getInstance(context) }

    suspend fun createProgress(request: CreateProgressDto): Response<ProgressResponseDto> = api.createProgress(request)
}
