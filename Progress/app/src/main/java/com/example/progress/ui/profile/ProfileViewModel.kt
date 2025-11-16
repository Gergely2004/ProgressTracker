package com.example.progress.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.HabitResponseDto
import com.example.progress.model.ProfileResponseDto
import com.example.progress.repository.ProfileRepository
import com.example.progress.repository.ScheduleRepository
import kotlinx.coroutines.launch

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProfileRepository(app)
    private val scheduleRepo = ScheduleRepository(app)

    private val _profile = MutableLiveData<ProfileResponseDto?>()
    val profile: LiveData<ProfileResponseDto?> = _profile

    private val _habits = MutableLiveData<List<HabitResponseDto>>()
    val habits: LiveData<List<HabitResponseDto>> = _habits

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // Fetch profile from repository
                val profileResponse = repo.getProfile()
                _profile.value = profileResponse
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profile"
                _profile.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHabits() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val habitsList = scheduleRepo.listHabits()
                _habits.value = habitsList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load habits"
                _habits.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
    }

}
