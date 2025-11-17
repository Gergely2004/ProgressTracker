package com.example.progress.repository

import android.content.Context
import com.example.progress.model.CreateHabitDto
import com.example.progress.model.HabitCategory
import com.example.progress.model.HabitResponseDto
import com.example.progress.network.RetrofitClient
import retrofit2.Response

class HabitRepository(context: Context) {
    private val api by lazy { RetrofitClient.getInstance(context) }

    suspend fun createHabit(request: CreateHabitDto): Response<HabitResponseDto> {
        return api.createHabit(request)
    }

    suspend fun listHabitCategories(): List<HabitCategory> {
        return api.listHabitCategories()
    }

    suspend fun listHabitsByUser(userId: Long): List<HabitResponseDto> {
        return api.listHabitsByUser(userId)
    }
}
