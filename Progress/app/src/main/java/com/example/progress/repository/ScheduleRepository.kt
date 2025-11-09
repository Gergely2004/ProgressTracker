package com.example.progress.repository

import android.content.Context
import com.example.progress.model.CreateCustomScheduleDto
import com.example.progress.model.CreateRecurringScheduleDto
import com.example.progress.model.CreateWeekdayRecurringDto
import com.example.progress.model.HabitResponseDto
import com.example.progress.model.ScheduleResponseDto
import com.example.progress.network.RetrofitClient
import retrofit2.Response

class ScheduleRepository(context: Context) {
    private val api by lazy { RetrofitClient.getInstance(context) }

    suspend fun getScheduleByDay(day: String): List<ScheduleResponseDto> {
        return api.getScheduleByDay(day)
    }

    suspend fun listHabits(): List<HabitResponseDto> = api.listHabits()

    suspend fun createCustomSchedule(request: CreateCustomScheduleDto): Response<ScheduleResponseDto> = api.createCustomSchedule(request)

    suspend fun createRecurringSchedule(request: CreateRecurringScheduleDto): Response<List<ScheduleResponseDto>> = api.createRecurringSchedule(request)

    suspend fun createWeekdayRecurringSchedule(request: CreateWeekdayRecurringDto): Response<List<ScheduleResponseDto>> = api.createWeekdayRecurring(request)
}