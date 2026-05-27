package com.agon.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.data.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UploadViewModel : ViewModel() {
    private val apiClient = ApiClient()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _taskId = MutableStateFlow<String?>(null)
    val taskId: StateFlow<String?> = _taskId.asStateFlow()

    fun uploadFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading(0)
            val result = apiClient.uploadFile(context, uri) { progress ->
                _uploadState.value = UploadState.Uploading(progress)
            }
            result.onSuccess { response ->
                _taskId.value = response.task_id
                _uploadState.value = UploadState.Completed
            }.onFailure { error ->
                _uploadState.value = UploadState.Error(error.message ?: "Upload failed")
            }
        }
    }

    fun getDownloadUrl(): String? {
        return _taskId.value?.let { apiClient.getDownloadUrl(it) }
    }

    fun getStreamUrl(): String? {
        return _taskId.value?.let { apiClient.getStreamUrl(it) }
    }

    fun reset() {
        _uploadState.value = UploadState.Idle
        _taskId.value = null
    }
}

sealed class UploadState {
    object Idle : UploadState()
    data class Uploading(val progress: Int) : UploadState()
    object Completed : UploadState()
    data class Error(val message: String) : UploadState()
}