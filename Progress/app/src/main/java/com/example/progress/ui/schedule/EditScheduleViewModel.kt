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

class EditScheduleViewModel(app: Application): AndroidViewModel(app) {
    private val repo = ScheduleRepository(app)

    private val _schedule = MutableLiveData<ScheduleResponseDto?>()
    val schedule: LiveData<ScheduleResponseDto?> = _schedule

    private val _updateResult = MutableLiveData<Result<ScheduleResponseDto>>()
    val updateResult: LiveData<Result<ScheduleResponseDto>> = _updateResult

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun load(id: Long) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                _schedule.postValue(repo.getScheduleById(id))
            } catch (e: Exception) {
                _schedule.postValue(null)
            } finally { _loading.postValue(false) }
        }
    }

    fun update(id: Long, dto: UpdateScheduleDto) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val resp = repo.updateSchedule(id, dto)
                if (resp.isSuccessful && resp.body()!=null) {
                    _updateResult.postValue(Result.success(resp.body()!!))
                    _schedule.postValue(resp.body()!!)
                } else {
                    val body = try { resp.errorBody()?.string() } catch (_:Exception){null}
                    _updateResult.postValue(Result.failure(Exception("Update failed ${resp.code()} ${body?:""}")))
                }
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e))
            } finally { _loading.postValue(false) }
        }
    }
}

