package com.example.progress.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
//import com.example.progress.model.AuthResponse
import com.example.progress.model.AuthResponseDto
import com.example.progress.repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)

    private val _registerResult = MutableLiveData<Result<AuthResponseDto>>()
    val registerResult: LiveData<Result<AuthResponseDto>> = _registerResult

    fun signup(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.signup(username, email, password)
                handleResponse(response)
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Signup failed", e)
                _registerResult.postValue(Result.failure(e))
            }
        }
    }

    private fun handleResponse(response: Response<AuthResponseDto>) {
        if (response.isSuccessful && response.body() != null) {
            _registerResult.postValue(Result.success(response.body()!!))
        } else {
            val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
            Log.e("RegisterViewModel", "Signup failed: code=${response.code()} body=$errorBody")
            val message = errorBody?.takeIf { it.isNotBlank() } ?: "Signup failed: ${response.code()}"
            _registerResult.postValue(Result.failure(Exception("Register failed: ${response.code()}")))
        }
    }
}
