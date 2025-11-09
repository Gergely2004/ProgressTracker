package com.example.progress.ui.habit

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.CreateHabitDto
import com.example.progress.model.HabitResponseDto
import com.example.progress.model.HabitCategory
import com.example.progress.repository.HabitRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class AddHabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HabitRepository(application)

    private val _createResult = MutableLiveData<Result<HabitResponseDto>>()
    val createResult: LiveData<Result<HabitResponseDto>> = _createResult

    private val _categories = MutableLiveData<List<HabitCategory>>()
    val categories: LiveData<List<HabitCategory>> = _categories

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCategories() {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val list = repository.listHabitCategories()
                _categories.postValue(list)
                _error.postValue(null)
            } catch (e: Exception) {
                Log.e("AddHabitViewModel", "Failed to load categories", e)
                _categories.postValue(emptyList())
                _error.postValue(e.message)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun createHabit(name: String, description: String?, categoryId: Long, goal: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val request = CreateHabitDto(name = name, description = description, categoryId = categoryId, goal = goal)
                val response = repository.createHabit(request)
                handleResponse(response)
            } catch (e: Exception) {
                Log.e("AddHabitViewModel", "create habit failed", e)
                _createResult.postValue(Result.failure(e))
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private fun handleResponse(response: Response<HabitResponseDto>) {
        if (response.isSuccessful && response.body() != null) {
            _createResult.postValue(Result.success(response.body()!!))
        } else {
            val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
            Log.e("AddHabitViewModel", "Create habit failed: code=${response.code()} body=$errorBody")
            _createResult.postValue(Result.failure(Exception("Create habit failed: ${response.code()}")))
        }
    }
}
