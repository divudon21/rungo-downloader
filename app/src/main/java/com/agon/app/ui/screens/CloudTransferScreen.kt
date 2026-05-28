package com.agon.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
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
                enabled = state is CloudTransferState.Idle || state is CloudTransferState.Error || (state is CloudTransferState.Transferring && (state as CloudTransferState.Transferring).status.status == "completed")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Transfer to GoFile")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (state) {
                is CloudTransferState.Idle -> {
                    Text("Server will download the file and automatically upload it to GoFile.")
                }
                is CloudTransferState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Starting transfer process...")
                }
                is CloudTransferState.Transferring -> {
                    val status = (state as CloudTransferState.Transferring).status
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Status: ${status.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", fontWeight = FontWeight.Bold)
                            
                            if (status.status == "downloading") {
                                val total = status.total_size ?: 0L
                                val downloaded = status.downloaded ?: 0L
                                val speed = status.speed ?: 0.0
                                val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
                                
                                Spacer(modifier = Modifier.height(8.dp))
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
                                Text("Uploading file from server to GoFile cloud...")
                            } else if (status.status == "completed") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Transfer completed!", color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val gofileUrl = status.gofile_url ?: ""
                                
                                Text("GoFile Link", style = MaterialTheme.typography.labelMedium)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    IconButton(onClick = { copyToClipboard(context, gofileUrl, "GoFile Link") }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                    }
                                    IconButton(onClick = { 
                                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(gofileUrl))
                                        context.startActivity(i) 
                                    }) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = "Open GoFile")
                                    }
                                    IconButton(onClick = { shareText(context, gofileUrl) }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share")
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
