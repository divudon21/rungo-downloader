package com.rungo.app.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient {
    private val baseUrl = "https://nayani7-ok.hf.space"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun startDownload(url: String): Result<DownloadResponse> {
        return try {
            val response = client.post("$baseUrl/start_download") {
                contentType(ContentType.Application.Json)
                setBody(DownloadRequest(url))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to start download: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStatus(taskId: String): Result<StatusResponse> {
        return try {
            val response = client.get("$baseUrl/status/$taskId")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to get status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getDownloadUrl(taskId: String): String {
        return "$baseUrl/download/$taskId"
    }
    
    fun getStreamUrl(taskId: String): String {
        return "$baseUrl/stream/$taskId"
    }
}
