package com.example.progress.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.ScheduleResponseDto
import com.example.progress.model.UpdateScheduleDto
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

    private val _deleteSuccess = MutableLiveData<Boolean>(false)
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

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

    fun updateNotes(id: Long, notes: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val res = repo.updateSchedule(id, UpdateScheduleDto(notes = notes))
                if (res.isSuccessful) {
                    val sched = repo.getScheduleById(id)
                    _schedule.postValue(sched)
                    _error.postValue(null)
                } else {
                    _error.postValue("Failed to update notes: ${res.code()} ${res.message()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun deleteSchedule(id: Long) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val res = repo.deleteSchedule(id)
                if (res.isSuccessful) {
                    _deleteSuccess.postValue(true)
                    _error.postValue(null)
                } else {
                    _error.postValue("Failed to delete schedule: ${res.code()} ${res.message()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _loading.postValue(false)
            }
        }
    }
}