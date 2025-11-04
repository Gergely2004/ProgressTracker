package com.example.progress.repository

import android.content.Context
import com.example.progress.model.CreateHabitDto
import com.example.progress.model.HabitResponse
import com.example.progress.network.RetrofitClient
import retrofit2.Response

class HabitRepository(context: Context) {
    private val api by lazy { RetrofitClient.getInstance(context) }

    suspend fun createHabit(request: CreateHabitDto): Response<HabitResponse> {
        return api.createHabit(request)
    }

}

