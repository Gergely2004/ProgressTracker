package com.example.progress.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.ProfileResponseDto
import com.example.progress.model.UpdateProfileDto
import com.example.progress.repository.ProfileRepository
import kotlinx.coroutines.launch

class EditProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProfileRepository(app)

    private val _profile = MutableLiveData<ProfileResponseDto?>()
    val profile: LiveData<ProfileResponseDto?> = _profile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    init {
        _isLoading.value = false
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
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

    fun updateProfile(username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _updateSuccess.value = false

                val updateDto = UpdateProfileDto(username = username.trim())
                val response = repo.updateProfile(updateDto)

                if (response.isSuccessful) {
                    _profile.value = response.body()
                    _updateSuccess.value = true
                } else {
                    _error.value = "Failed to update profile: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update profile"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

