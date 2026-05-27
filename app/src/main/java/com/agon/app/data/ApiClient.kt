package com.agon.app.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.InputStream

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
    
    suspend fun getStorage(): Result<StorageResponse> {
        return try {
            val response = client.get("$baseUrl/storage")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to get storage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAll(): Result<Unit> {
        return try {
            val response = client.post("$baseUrl/delete_all")
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete all"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(): Result<HistoryResponse> {
        return try {
            val response = client.get("$baseUrl/history")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to get history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(context: Context, uri: Uri, onProgress: (Int) -> Unit): Result<DownloadResponse> {
        return try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(context, uri) ?: "upload.bin"
            val inputStream = contentResolver.openInputStream(uri) ?: return Result.failure(Exception("Could not open file"))
            val bytes = inputStream.readBytes()
            inputStream.close()

            val response = client.submitFormWithBinaryData(
                url = "$baseUrl/upload",
                formData = formData {
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ) {
                onUpload { bytesSentTotal, contentLength ->
                    if (contentLength > 0) {
                        val progress = (bytesSentTotal * 100 / contentLength).toInt()
                        onProgress(progress)
                    }
                }
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to upload file: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    suspend fun startGoFileTransfer(url: String): Result<DownloadResponse> {
        return try {
            val response = client.post("$baseUrl/start_gofile_transfer") {
                contentType(ContentType.Application.Json)
                setBody(DownloadRequest(url))
            }
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to start GoFile transfer: ${response.status}"))
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