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
import com.agon.app.viewmodel.CloudTransferViewModel
import com.agon.app.viewmodel.TransferState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudTransferScreen(viewModel: CloudTransferViewModel = viewModel()) {
    val state by viewModel.transferState.collectAsState()
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cloud Transfer (GoFile)") })
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
                label = { Text("Enter URL to transfer to GoFile") },
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
                enabled = state is TransferState.Idle || state is TransferState.Error || (state is TransferState.Transferring && (state as TransferState.Transferring).status.status == "completed")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Transfer to GoFile")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (state) {
                is TransferState.Idle -> {
                    Text("Server will download the file and directly upload it to GoFile.")
                }
                is TransferState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Starting transfer task...")
                }
                is TransferState.Transferring -> {
                    val status = (state as TransferState.Transferring).status
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val displayStatus = when(status.status) {
                                "downloading" -> "Downloading to Server..."
                                "uploading_to_gofile" -> "Uploading to GoFile..."
                                "completed" -> "Completed!"
                                else -> status.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            }
                            
                            Text("Status: $displayStatus", fontWeight = FontWeight.Bold)
                            
                            if (status.status == "downloading") {
                                Spacer(modifier = Modifier.height(8.dp))
                                val total = status.total_size ?: 0L
                                val downloaded = status.downloaded ?: 0L
                                val speed = status.speed ?: 0.0
                                val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
                                
                                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(formatSize(downloaded) + " / " + formatSize(total))
                                    Text(formatSpeed(speed))
                                }
                            } else if (status.status == "uploading_to_gofile") {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Please wait, sending file to GoFile servers...")
                            } else if (status.status == "completed") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("File transferred to GoFile successfully!", color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val gofileUrl = status.gofile_url ?: ""
                                
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
                                    Button(onClick = { shareLink(context, gofileUrl) }) {
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
                is TransferState.Error -> {
                    Text((state as TransferState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
