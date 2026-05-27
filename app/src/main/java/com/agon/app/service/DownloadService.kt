package com.agon.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.agon.app.data.ApiClient
import kotlinx.coroutines.*

class DownloadService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val apiClient = ApiClient()
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskId = intent?.getStringExtra("TASK_ID") ?: return START_NOT_STICKY

        createNotificationChannel()

        val notification = buildNotification("Starting download...", 0, 0)
        startForeground(taskId.hashCode(), notification)

        pollStatus(taskId, startId)

        return START_STICKY
    }

    private fun pollStatus(taskId: String, startId: Int) {
        serviceScope.launch {
            while (isActive) {
                val result = apiClient.getStatus(taskId)
                if (result.isSuccess) {
                    val status = result.getOrNull()!!
                    if (status.status == "downloading") {
                        val total = status.total_size ?: 0L
                        val downloaded = status.downloaded ?: 0L
                        val progress = if (total > 0) (downloaded * 100 / total).toInt() else 0
                        val notification = buildNotification("Downloading: $progress%", total.toInt(), downloaded.toInt())
                        notificationManager.notify(taskId.hashCode(), notification)
                    } else if (status.status == "completed") {
                        val notification = buildNotification("Download Complete", 0, 0).apply {
                            notificationManager.notify(taskId.hashCode(), this)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            stopForeground(STOP_FOREGROUND_DETACH)
                        } else {
                            stopForeground(false)
                        }
                        stopSelf(startId)
                        break
                    } else if (status.status == "error") {
                        val notification = buildNotification("Download Error", 0, 0).apply {
                            notificationManager.notify(taskId.hashCode(), this)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            stopForeground(STOP_FOREGROUND_DETACH)
                        } else {
                            stopForeground(false)
                        }
                        stopSelf(startId)
                        break
                    }
                }
                delay(1000)
            }
        }
    }

    private fun buildNotification(text: String, max: Int, progress: Int): Notification {
        return NotificationCompat.Builder(this, "download_channel")
            .setContentTitle("Rungo Downloader")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(max, progress, max == 0 && progress == 0 && text.contains("Starting"))
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("download_channel", "Downloads", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}