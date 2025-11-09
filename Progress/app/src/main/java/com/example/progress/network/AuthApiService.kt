package com.example.progress.network

import com.example.progress.model.AuthRequest
import com.example.progress.model.AuthResponseDto
import com.example.progress.model.ScheduleResponseDto
import com.example.progress.model.CreateHabitDto
import com.example.progress.model.CreateCustomScheduleDto
import com.example.progress.model.CreateRecurringScheduleDto
import com.example.progress.model.HabitResponseDto
import com.example.progress.model.CreateWeekdayRecurringDto
import com.example.progress.model.HabitCategory
import com.example.progress.model.CreateProgressDto
import com.example.progress.model.ProgressResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("/auth/local/signin")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponseDto>

    @POST("/auth/local/signup")
    suspend fun signup(@Body request: AuthRequest): Response<AuthResponseDto>

    @GET("/schedule/day")
    suspend fun getScheduleByDay(@Query("date") day: String): List<ScheduleResponseDto>

    // List habits
    @GET("/habit")
    suspend fun listHabits(): List<HabitResponseDto>

    // List habit categories
    @GET("/habit/categories")
    suspend fun listHabitCategories(): List<HabitCategory>

    @POST("/habit")
    suspend fun createHabit(@Body request: CreateHabitDto): Response<HabitResponseDto>

    // Create custom schedule
    @POST("/schedule/custom")
    suspend fun createCustomSchedule(@Body request: CreateCustomScheduleDto): Response<ScheduleResponseDto>

    // Create recurring schedules (generic) - keeping for compatibility
    @POST("/schedule/recurring")
    suspend fun createRecurringSchedule(@Body request: CreateRecurringScheduleDto): Response<List<ScheduleResponseDto>>

    // Create weekday recurring schedules
    @POST("/schedule/recurring/weekdays")
    suspend fun createWeekdayRecurring(@Body request: CreateWeekdayRecurringDto): Response<List<ScheduleResponseDto>>

    @POST("/progress")
    suspend fun createProgress(@Body request: CreateProgressDto): Response<ProgressResponseDto>
}