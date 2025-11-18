package com.example.progress.ui.schedule

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.CreateHabitDto
import com.example.progress.model.CreateCustomScheduleDto
import com.example.progress.model.CreateRecurringScheduleDto
import com.example.progress.model.CreateWeekdayRecurringDto
import com.example.progress.model.HabitResponseDto
import com.example.progress.model.HabitCategory
import com.example.progress.repository.HabitRepository
import com.example.progress.repository.ScheduleRepository
import kotlinx.coroutines.launch

class CreateScheduleViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ScheduleRepository(app)
    private val habitRepo = HabitRepository(app)

    private val _habits = MutableLiveData<List<HabitResponseDto>>()
    val habits: LiveData<List<HabitResponseDto>> = _habits

    private val _categories = MutableLiveData<List<HabitCategory>>()
    val categories: LiveData<List<HabitCategory>> = _categories

    private val _createResult = MutableLiveData<Result<Unit>>()
    val createResult: LiveData<Result<Unit>> = _createResult

    fun loadHabits() {
        viewModelScope.launch {
            try {
                _habits.postValue(repo.listHabits())
            } catch (e: Exception) {
                Log.e("CreateScheduleVM", "Failed to load habits", e)
                _habits.postValue(emptyList())
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val list = habitRepo.listHabitCategories()
                _categories.postValue(list)
            } catch (e: Exception) {
                Log.e("CreateScheduleVM", "Failed to load categories", e)
                _categories.postValue(emptyList())
            }
        }
    }

    fun resolveSelectedHabitId(selectedName: CharSequence?): Long? {
        val name = selectedName?.toString()?.trim().orEmpty()
        return _habits.value?.firstOrNull { it.name == name }?.id
    }

    fun createScheduleCustom(date: String, startTime: String, notes: String?, duration: Int?, habitId: Long) {
        viewModelScope.launch {
            try {
                val req = CreateCustomScheduleDto(
                    habitId = habitId,
                    date = date,
                    startTime = startTime,
                    endTime = null,
                    durationMinutes = duration,
                    isCustom = true,
                    participantIds = null,
                    notes = notes
                )
                val resp = repo.createCustomSchedule(req)
                if (resp.isSuccessful) {
                    _createResult.postValue(Result.success(Unit))
                } else {
                    val body = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Create failed ${resp.code()} ${body ?: ""}")))
                }
            } catch (e: Exception) {
                _createResult.postValue(Result.failure(e))
            }
        }
    }

    fun createHabitThenCustomSchedule(
        name: String,
        description: String?,
        categoryId: Long,
        goal: String,
        date: String,
        startTime: String,
        notes: String?,
        duration: Int?
    ) {
        viewModelScope.launch {
            try {
                // First create the habit
                val habitRepo = HabitRepository(getApplication())
                val createResp = habitRepo.createHabit(CreateHabitDto(name, description, categoryId, goal))
                if (!createResp.isSuccessful || createResp.body() == null) {
                    val body = try { createResp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Habit creation failed ${createResp.code()} ${body ?: ""}")))
                    return@launch
                }
                val habitId = createResp.body()!!.id
                // Then create the custom schedule
                val req = CreateCustomScheduleDto(
                    habitId = habitId,
                    date = date,
                    startTime = startTime,
                    endTime = null,
                    durationMinutes = duration,
                    isCustom = true,
                    participantIds = null,
                    notes = notes
                )
                val schedResp = repo.createCustomSchedule(req)
                if (schedResp.isSuccessful) {
                    _createResult.postValue(Result.success(Unit))
                } else {
                    val body2 = try { schedResp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Create failed ${schedResp.code()} ${body2 ?: ""}")))
                }
            } catch (e: Exception) {
                _createResult.postValue(Result.failure(e))
            }
        }
    }

    fun createRecurringSchedule(
        habitId: Long,
        startTime: String,
        repeatPattern: String,
        duration: Int?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val req = CreateRecurringScheduleDto(
                    habitId = habitId,
                    startTime = startTime,
                    endTime = null,
                    durationMinutes = duration,
                    repeatPattern = repeatPattern,
                    repeatDays = 30,
                    isCustom = true,
                    participantIds = null,
                    notes = notes
                )
                val resp = repo.createRecurringSchedule(req)
                if (resp.isSuccessful) {
                    _createResult.postValue(Result.success(Unit))
                } else {
                    val body = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Recurring create failed ${resp.code()} ${body ?: ""}")))
                }
            } catch (e: Exception) {
                _createResult.postValue(Result.failure(e))
            }
        }
    }

    fun createWeekdayRecurringSchedule(
        habitId: Long,
        startTime: String,
        daysOfWeek: List<Int>,
        numberOfWeeks: Int,
        duration: Int?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val req = CreateWeekdayRecurringDto(
                    habitId = habitId,
                    startTime = startTime,
                    durationMinutes = duration,
                    endTime = null,
                    daysOfWeek = daysOfWeek,
                    numberOfWeeks = numberOfWeeks,
                    participantIds = null,
                    notes = notes
                )
                val resp = repo.createWeekdayRecurringSchedule(req)
                if (resp.isSuccessful) {
                    _createResult.postValue(Result.success(Unit))
                } else {
                    val body = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Weekday recurring failed ${resp.code()} ${body ?: ""}")))
                }
            } catch (e: Exception) {
                _createResult.postValue(Result.failure(e))
            }
        }
    }

    fun createHabitThenRecurringSchedule(
        name: String,
        description: String?,
        categoryId: Long,
        goal: String,
        startTime: String,
        repeatPattern: String,
        duration: Int?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val habitRepo = HabitRepository(getApplication())
                val createResp = habitRepo.createHabit(CreateHabitDto(name, description, categoryId, goal))
                if (!createResp.isSuccessful || createResp.body() == null) {
                    val body = try { createResp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Habit creation failed ${createResp.code()} ${body ?: ""}")))
                    return@launch
                }
                val habitId = createResp.body()!!.id
                val req = CreateRecurringScheduleDto(
                    habitId = habitId,
                    startTime = startTime,
                    endTime = null,
                    durationMinutes = duration,
                    repeatPattern = repeatPattern,
                    repeatDays = 30,
                    isCustom = true,
                    participantIds = null,
                    notes = notes
                )
                val resp = repo.createRecurringSchedule(req)
                if (resp.isSuccessful) {
                    _createResult.postValue(Result.success(Unit))
                } else {
                    val body = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Recurring create failed ${resp.code()} ${body ?: ""}")))
                }
            } catch (e: Exception) {
                _createResult.postValue(Result.failure(e))
            }
        }
    }

    fun createHabitThenWeekdayRecurringSchedule(
        name: String,
        description: String?,
        categoryId: Long,
        goal: String,
        startTime: String,
        daysOfWeek: List<Int>,
        numberOfWeeks: Int,
        duration: Int?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val habitRepo = HabitRepository(getApplication())
                val createResp = habitRepo.createHabit(CreateHabitDto(name, description, categoryId, goal))
                if (!createResp.isSuccessful || createResp.body() == null) {
                    val body = try { createResp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Habit creation failed ${createResp.code()} ${body ?: ""}")))
                    return@launch
                }
                val habitId = createResp.body()!!.id
                val req = CreateWeekdayRecurringDto(
                    habitId = habitId,
                    startTime = startTime,
                    durationMinutes = duration,
                    endTime = null,
                    daysOfWeek = daysOfWeek,
                    numberOfWeeks = numberOfWeeks,
                    participantIds = null,
                    notes = notes
                )
                val resp = repo.createWeekdayRecurringSchedule(req)
                if (resp.isSuccessful) {
                    _createResult.postValue(Result.success(Unit))
                } else {
                    val body = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Weekday recurring failed ${resp.code()} ${body ?: ""}")))
                }
            } catch (e: Exception) {
                _createResult.postValue(Result.failure(e))
            }
        }
    }
}
