package com.example.progress.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.ScheduleResponseDto
import com.example.progress.repository.ScheduleRepository
import kotlinx.coroutines.launch

class ScheduleDetailsViewModel(app: Application): AndroidViewModel(app) {
    private val repo = ScheduleRepository(app)

    private val _schedule = MutableLiveData<ScheduleResponseDto?>()
    val schedule: LiveData<ScheduleResponseDto?> = _schedule

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun load(id: Long) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val sched = repo.getScheduleById(id)
                _schedule.postValue(sched)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _loading.postValue(false)
            }
        }
    }
}