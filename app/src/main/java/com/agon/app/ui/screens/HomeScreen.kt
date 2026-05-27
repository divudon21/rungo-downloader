package com.agon.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rungo.com.viewmodel.DownloadState
import com.rungo.com.viewmodel.DownloadViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DownloadViewModel = viewModel()
) {
    val state by viewModel.downloadState.collectAsState()
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rungo Downloader") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                        viewModel.startDownload(urlInput)
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
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(onClick = { copyToClipboard(context, downloadUrl, "Download Link") }) {
                                        Icon(Icons.Default.Download, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Copy Download Link")
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(onClick = { copyToClipboard(context, streamUrl, "Stream Link") }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Copy Stream Link")
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
