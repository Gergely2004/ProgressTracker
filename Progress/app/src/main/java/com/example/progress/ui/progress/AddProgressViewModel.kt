package com.example.progress.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.CreateProgressDto
import com.example.progress.model.ProgressResponseDto
import com.example.progress.repository.ProgressRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddProgressViewModel(app: Application): AndroidViewModel(app) {
    private val repo = ProgressRepository(app)

    private val _createResult = MutableLiveData<Result<ProgressResponseDto>>()
    val createResult: LiveData<Result<ProgressResponseDto>> = _createResult

    fun submitProgress(scheduleId: Long, loggedMinutes: Double, notes: String?, completed: Boolean, dateOverride: String? = null) {
        viewModelScope.launch {
            try {
                val dateStr = dateOverride ?: LocalDate.now().toString()
                val dto = CreateProgressDto(
                    scheduleId = scheduleId,
                    date = dateStr,
                    loggedTime = loggedMinutes,
                    notes = notes,
                    isCompleted = completed
                )
                val resp = repo.createProgress(dto)
                if (resp.isSuccessful && resp.body() != null) {
                    _createResult.postValue(Result.success(resp.body()!!))
                } else {
                    val body = try { resp.errorBody()?.string() } catch (_: Exception) { null }
                    _createResult.postValue(Result.failure(Exception("Progress create failed ${resp.code()} ${body ?: ""}")))
                }
            } catch (e: Exception) {
                _createResult.postValue(Result.failure(e))
            }
        }
    }
}
