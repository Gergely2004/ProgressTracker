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

class AuthViewModel(application: Application) :
    AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    private val _authResult = MutableLiveData<Result<AuthResponseDto>>()
    val authResult: LiveData<Result<AuthResponseDto>> = _authResult
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                handleResponse(response)
            } catch (e: Exception) {
                _authResult.postValue(Result.failure(e))
            }
        }
    }
    private fun handleResponse(response: Response<AuthResponseDto>) {
        if (response.isSuccessful && response.body() != null) {
            _authResult.postValue(Result.success(response.body()!!))
        } else {
            _authResult.postValue(Result.failure(Exception("Auth failed: ${response.code()}")))
        }
    }
}
