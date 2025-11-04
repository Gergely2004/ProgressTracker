package com.example.progress.ui.habit

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.CreateHabitDto
import com.example.progress.model.HabitResponse
import com.example.progress.repository.HabitRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class AddHabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository(application)

    private val _createResult = MutableLiveData<Result<HabitResponse>>()
    val createResult: LiveData<Result<HabitResponse>> = _createResult

    fun createHabit(name: String, description: String?, categoryId: Long, goal: String) {
        viewModelScope.launch {
            try {
                val request = CreateHabitDto(name = name, description = description, categoryId = categoryId, goal = goal)
                val response = repository.createHabit(request)
                handleResponse(response)
            } catch (e: Exception) {
                Log.e("AddHabitViewModel", "create habit failed", e)
                _createResult.postValue(Result.failure(e))
            }
        }
    }

    private fun handleResponse(response: Response<HabitResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _createResult.postValue(Result.success(response.body()!!))
        } else {
            val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
            Log.e("AddHabitViewModel", "Create habit failed: code=${response.code()} body=$errorBody")
            _createResult.postValue(Result.failure(Exception("Create habit failed: ${response.code()}")))
        }
    }
}
