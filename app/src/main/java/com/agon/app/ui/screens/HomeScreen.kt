package com.agon.app.ui.screens

import androidx.compose.material.icons.filled.ContentCopy
import android.net.Uri
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agon.app.service.DownloadService
import com.agon.app.viewmodel.DownloadState
import com.agon.app.viewmodel.DownloadViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DownloadViewModel = viewModel()
) {
    val state by viewModel.downloadState.collectAsState()
    val storage by viewModel.storageState.collectAsState()
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchStorage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FastGo") },
                actions = {
                    IconButton(onClick = { viewModel.deleteAll() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete All Files")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (storage != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Server Storage", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val usedGb = storage!!.used / (1024.0 * 1024 * 1024)
                        val totalGb = storage!!.total / (1024.0 * 1024 * 1024)
                        val freeGb = storage!!.free / (1024.0 * 1024 * 1024)
                        Text(String.format(Locale.US, "Used: %.2f GB / %.2f GB", usedGb, totalGb))
                        Text(String.format(Locale.US, "Free: %.2f GB", freeGb))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Enter URL to download") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    if (urlInput.isNotBlank()) {
                        viewModel.startDownload(urlInput) { taskId ->
                            val intent = Intent(context, DownloadService::class.java).apply {
                                putExtra("TASK_ID", taskId)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state is DownloadState.Idle || state is DownloadState.Error || (state is DownloadState.Downloading && (state as DownloadState.Downloading).status.status == "completed")
            ) {
                Text("Download to Server")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (state) {
                is DownloadState.Idle -> {
                    Text("Enter a URL to start downloading to the Hugging Face server.")
                }
                is DownloadState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Starting download...")
                }
                is DownloadState.Downloading -> {
                    val status = (state as DownloadState.Downloading).status
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Status: ${status.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", fontWeight = FontWeight.Bold)
                            
                            if (status.status == "downloading") {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val total = status.total_size ?: 0L
                                val downloaded = status.downloaded ?: 0L
                                val speed = status.speed ?: 0.0
                                
                                val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
                                
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(formatSize(downloaded) + " / " + formatSize(total))
                                    Text(formatSpeed(speed))
                                }
                            } else if (status.status == "completed") {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text("File downloaded successfully to server!", color = MaterialTheme.colorScheme.primary)
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val downloadUrl = viewModel.getDownloadUrl() ?: ""
                                val streamUrl = viewModel.getStreamUrl() ?: ""
                                
                            Text("Download Link", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { copyToClipboard(context, downloadUrl, "Download Link") }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy", maxLines = 1)
                                }
                                Button(onClick = { 
                                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                    context.startActivity(i) 
                                }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open", maxLines = 1)
                                }
                                Button(onClick = { shareLink(context, downloadUrl) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share", maxLines = 1)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Stream Link", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { copyToClipboard(context, streamUrl, "Stream Link") }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy", maxLines = 1)
                                }
                                Button(onClick = { 
                                    openStreamInPlayer(context, streamUrl, status.original_filename)
                                }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open", maxLines = 1)
                                }
                                Button(onClick = { shareLink(context, streamUrl) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share", maxLines = 1)
                                }
                            }
                            } else if (status.status == "error") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Error: ${status.error}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                is DownloadState.Error -> {
                    Text((state as DownloadState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

fun openStreamInPlayer(context: Context, streamUrl: String, filename: String?) {
    val intent = Intent(Intent.ACTION_VIEW)
    
    // Guess MIME type or default to video/* to force external media players like MX Player / VLC
    var mimeType = "video/*"
    if (filename != null) {
        val ext = filename.substringAfterLast('.', "").lowercase(Locale.getDefault())
        mimeType = when (ext) {
            "mp3", "wav", "ogg", "m4a", "flac", "aac" -> "audio/*"
            "mp4", "mkv", "webm", "avi", "mov", "flv" -> "video/*"
            "jpg", "jpeg", "png", "gif", "webp" -> "image/*"
            "pdf" -> "application/pdf"
            "apk" -> "application/vnd.android.package-archive"
            "zip", "rar", "7z" -> "application/zip"
            else -> "video/*"
        }
    }
    
    intent.setDataAndType(Uri.parse(streamUrl), mimeType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    
    try {
        context.startActivity(Intent.createChooser(intent, "Open Stream with..."))
    } catch (e: Exception) {
        // Fallback to normal URL opening if no player is found
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl))
        try {
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "No app found to open this link", Toast.LENGTH_SHORT).show()
        }
    }
}

fun shareLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(Intent.createChooser(intent, "Share Link"))
}

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun formatSpeed(bytesPerSec: Double): String {
    if (bytesPerSec <= 0) return "0 B/s"
    val units = arrayOf("B/s", "KB/s", "MB/s", "GB/s")
    val digitGroups = (Math.log10(bytesPerSec) / Math.log10(1024.0)).toInt()
    return String.format(Locale.getDefault(), "%.1f %s", bytesPerSec / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}