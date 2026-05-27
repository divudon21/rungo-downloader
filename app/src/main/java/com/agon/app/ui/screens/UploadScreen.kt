package com.agon.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agon.app.viewmodel.UploadState
import com.agon.app.viewmodel.UploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(viewModel: UploadViewModel = viewModel()) {
    val state by viewModel.uploadState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadFile(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Local Upload") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { launcher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = state is UploadState.Idle || state is UploadState.Error || state is UploadState.Completed
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select File to Upload")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (state) {
                is UploadState.Idle -> {
                    Text("Select a file from your device to upload to the server.")
                }
                is UploadState.Uploading -> {
                    val progress = (state as UploadState.Uploading).progress
                    Text("Uploading...", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$progress%")
                }
                is UploadState.Completed -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("File uploaded successfully!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val downloadUrl = viewModel.getDownloadUrl() ?: ""
                            val streamUrl = viewModel.getStreamUrl() ?: ""
                            
                            Text("Download Link", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = { copyToClipboard(context, downloadUrl, "Download Link") }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy")
                                }
                                Button(onClick = { 
                                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                    context.startActivity(i) 
                                }) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Stream Link", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = { copyToClipboard(context, streamUrl, "Stream Link") }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy")
                                }
                                Button(onClick = { 
                                    // Make sure openStreamInPlayer is accessible or duplicate it here.
                                    // Since it's in HomeScreen.kt we can just call it.
                                    openStreamInPlayer(context, streamUrl, "video.mp4") // Defaulting to video to trigger player
                                }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open")
                                }
                            }
                        }
                    }
                }
                is UploadState.Error -> {
                    Text((state as UploadState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
