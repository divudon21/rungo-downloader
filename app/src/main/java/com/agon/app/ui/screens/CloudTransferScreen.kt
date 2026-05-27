package com.agon.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agon.app.viewmodel.CloudTransferState
import com.agon.app.viewmodel.CloudTransferViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudTransferScreen(viewModel: CloudTransferViewModel = viewModel()) {
    val state by viewModel.transferState.collectAsState()
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cloud Transfer") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Download to Server -> Upload to GoFile",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Enter Direct URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    if (urlInput.isNotBlank()) {
                        viewModel.startTransfer(urlInput)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state is CloudTransferState.Idle || state is CloudTransferState.Error || (state is CloudTransferState.Processing && (state as CloudTransferState.Processing).status.status == "completed")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start Transfer")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (state) {
                is CloudTransferState.Idle -> {
                    Text("Enter a URL. The server will download it and automatically upload it to GoFile for you.")
                }
                is CloudTransferState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Starting transfer process...")
                }
                is CloudTransferState.Processing -> {
                    val status = (state as CloudTransferState.Processing).status
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val statusText = when(status.status) {
                                "downloading" -> "Downloading to Server..."
                                "uploading_to_gofile" -> "Uploading to GoFile..."
                                "completed" -> "Transfer Complete!"
                                else -> status.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            }
                            
                            Text("Status: $statusText", fontWeight = FontWeight.Bold, color = if(status.status == "completed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            
                            if (status.status == "downloading" || status.status == "uploading_to_gofile") {
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
                                    if (status.status == "downloading") {
                                        Text(formatSpeed(speed))
                                    }
                                }
                            } else if (status.status == "completed") {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val gofileUrl = status.gofile_url ?: ""
                                
                                Text("GoFile Link", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(onClick = { copyToClipboard(context, gofileUrl, "GoFile Link") }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Copy")
                                    }
                                    Button(onClick = { 
                                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(gofileUrl))
                                        context.startActivity(i) 
                                    }) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Open")
                                    }
                                    Button(onClick = { 
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Here is my file: $gofileUrl")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share GoFile Link"))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Share")
                                    }
                                }
                            } else if (status.status == "error") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Error: ${status.error}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                is CloudTransferState.Error -> {
                    Text((state as CloudTransferState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
