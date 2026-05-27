package com.agon.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.data.ApiClient
import com.agon.app.data.StatusResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val apiClient = ApiClient()

    private val _historyState = MutableStateFlow<List<StatusResponse>>(emptyList())
    val historyState: StateFlow<List<StatusResponse>> = _historyState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = apiClient.getHistory()
            if (result.isSuccess) {
                _historyState.value = result.getOrNull()?.history ?: emptyList()
            }
            _isLoading.value = false
        }
    }
}