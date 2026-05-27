package com.agon.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.data.ApiClient
import com.agon.app.data.StatusResponse
import com.agon.app.data.StorageResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadViewModel : ViewModel() {
    private val apiClient = ApiClient()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _taskId = MutableStateFlow<String?>(null)
    val taskId: StateFlow<String?> = _taskId.asStateFlow()

    private val _storageState = MutableStateFlow<StorageResponse?>(null)
    val storageState: StateFlow<StorageResponse?> = _storageState.asStateFlow()

    init {
        fetchStorage()
    }

    fun fetchStorage() {
        viewModelScope.launch {
            val result = apiClient.getStorage()
            if (result.isSuccess) {
                _storageState.value = result.getOrNull()
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            apiClient.deleteAll()
            fetchStorage()
            reset()
        }
    }

    fun startDownload(url: String, onStartService: (String) -> Unit) {
        viewModelScope.launch {
            _downloadState.value = DownloadState.Loading
            val result = apiClient.startDownload(url)
            result.onSuccess { response ->
                _taskId.value = response.task_id
                onStartService(response.task_id)
                pollStatus(response.task_id)
            }.onFailure { error ->
                _downloadState.value = DownloadState.Error(error.message ?: "Failed to start download")
            }
        }
    }

    private fun pollStatus(taskId: String) {
        viewModelScope.launch {
            while (true) {
                val result = apiClient.getStatus(taskId)
                result.onSuccess { status ->
                    _downloadState.value = DownloadState.Downloading(status)
                    if (status.status == "completed" || status.status == "error") {
                        fetchStorage()
                        return@launch
                    }
                }.onFailure { error ->
                    _downloadState.value = DownloadState.Error(error.message ?: "Failed to get status")
                    return@launch
                }
                delay(1000)
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
        _downloadState.value = DownloadState.Idle
        _taskId.value = null
    }
}

sealed class DownloadState {
    object Idle : DownloadState()
    object Loading : DownloadState()
    data class Downloading(val status: StatusResponse) : DownloadState()
    data class Error(val message: String) : DownloadState()
}