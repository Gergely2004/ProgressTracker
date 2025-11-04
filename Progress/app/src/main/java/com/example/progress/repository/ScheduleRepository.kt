package com.example.progress.repository

import android.content.Context
import com.example.progress.model.CreateCustomScheduleDto
import com.example.progress.model.HabitResponse
import com.example.progress.model.ScheduleResponseDto
import com.example.progress.network.RetrofitClient
import retrofit2.Response

class ScheduleRepository(context: Context) {
    private val api by lazy { RetrofitClient.getInstance(context) }

    suspend fun getScheduleByDay(day: String): List<ScheduleResponseDto> {
        return api.getScheduleByDay(day)
    }

    suspend fun listHabits(): List<HabitResponse> = api.listHabits()

    suspend fun createCustomSchedule(request: CreateCustomScheduleDto): Response<ScheduleResponseDto> = api.createCustomSchedule(request)
}