package com.rungo.com.data

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
    val url: String? = null,
    val status: String,
    val total_size: Long? = null,
    val downloaded: Long? = null,
    val speed: Double? = null,
    val error: String? = null
)
