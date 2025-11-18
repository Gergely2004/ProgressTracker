package com.example.progress.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.progress.model.ProfileResponseDto
import com.example.progress.model.UpdateProfileDto
import com.example.progress.repository.ProfileRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class EditProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProfileRepository(app)
    private val context = app.applicationContext

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

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            var tempFile: File? = null
            try {
                _isLoading.value = true
                _error.value = null
                _updateSuccess.value = false

                android.util.Log.d("EditProfileVM", "Uploading image from URI: $imageUri")
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw IllegalArgumentException("Cannot open image")

                tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

                android.util.Log.d("EditProfileVM", "Uploading file: ${tempFile.name}, size: ${tempFile.length()} bytes, field: file")
                val response = repo.uploadProfileImage(imagePart)

                if (response.isSuccessful) {
                    _profile.value = response.body()
                    _updateSuccess.value = true
                    android.util.Log.d("EditProfileVM", "Upload successful")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = "Failed to upload profile image: ${response.code()} - ${errorBody?.take(200) ?: ""}"
                    android.util.Log.e("EditProfileVM", errorMsg)
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                android.util.Log.e("EditProfileVM", "Upload exception", e)
                _error.value = e.message ?: "Failed to upload profile image"
            } finally {
                tempFile?.delete()
                _isLoading.value = false
            }
        }
    }
}
