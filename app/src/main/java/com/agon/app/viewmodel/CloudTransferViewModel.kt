package com.agon.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.data.ApiClient
import com.agon.app.data.StatusResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CloudTransferViewModel : ViewModel() {
    private val apiClient = ApiClient()

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val _taskId = MutableStateFlow<String?>(null)
    val taskId: StateFlow<String?> = _taskId.asStateFlow()

    fun startTransfer(url: String) {
        viewModelScope.launch {
            _transferState.value = TransferState.Loading
            val result = apiClient.startGoFileTransfer(url)
            result.onSuccess { response ->
                _taskId.value = response.task_id
                pollStatus(response.task_id)
            }.onFailure { error ->
                _transferState.value = TransferState.Error(error.message ?: "Failed to start transfer")
            }
        }
    }

    private fun pollStatus(taskId: String) {
        viewModelScope.launch {
            while (true) {
                val result = apiClient.getStatus(taskId)
                result.onSuccess { status ->
                    _transferState.value = TransferState.Transferring(status)
                    if (status.status == "completed" || status.status == "error") {
                        return@launch
                    }
                }.onFailure { error ->
                    _transferState.value = TransferState.Error(error.message ?: "Failed to get status")
                    return@launch
                }
                delay(1000)
            }
        }
    }

    fun reset() {
        _transferState.value = TransferState.Idle
        _taskId.value = null
    }
}

sealed class TransferState {
    object Idle : TransferState()
    object Loading : TransferState()
    data class Transferring(val status: StatusResponse) : TransferState()
    data class Error(val message: String) : TransferState()
}