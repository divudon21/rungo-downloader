package com.agon.app.data

import kotlinx.serialization.Serializable

@Serializable
data class DownloadRequest(
    val url: String
)

@Serializable
data class DownloadResponse(
    val task_id: String
)

@Serializable
data class StatusResponse(
    val task_id: String? = null,
    val url: String? = null,
    val status: String,
    val total_size: Long? = null,
    val downloaded: Long? = null,
    val speed: Double? = null,
    val error: String? = null,
    val timestamp: Double? = null
)

@Serializable
data class StorageResponse(
    val total: Long,
    val used: Long,
    val free: Long
)

@Serializable
data class HistoryResponse(
    val history: List<StatusResponse>
)

@Serializable
data class DeleteResponse(
    val status: String
)