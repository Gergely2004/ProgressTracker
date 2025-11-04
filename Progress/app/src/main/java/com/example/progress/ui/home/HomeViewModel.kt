package com.example.progress.ui.home

import androidx.lifecycle.*
import com.example.progress.model.ScheduleResponseDto
import com.example.progress.repository.ScheduleRepository
import kotlinx.coroutines.launch
/**
 * ViewModel that manages schedule-related data for the ScheduleFragment.
 */
class HomeViewModel(private val repository: ScheduleRepository) :
    ViewModel() {
    private val _schedules = MutableLiveData<List<ScheduleResponseDto>>()
    val schedules: LiveData<List<ScheduleResponseDto>> get() = _schedules
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    fun getScheduleByDay(day: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getScheduleByDay(day)
                val sorted = response.sortedWith(compareBy(nullsLast()) { it.startTime })
                _schedules.value = sorted
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load schedules"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}