package com.agon.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agon.app.data.ApiClient
import com.agon.app.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel()) {
    val history by viewModel.historyState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val apiClient = ApiClient()

    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download History") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (history.isEmpty()) {
                Text("No download history.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(item.url ?: "Unknown URL", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Status: ${item.status}")
                                if (item.total_size != null) {
                                    Text("Size: ${formatSize(item.total_size)}")
                                }
                                if (item.timestamp != null) {
                                    val date = Date((item.timestamp * 1000).toLong())
                                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    Text("Date: ${format.format(date)}", style = MaterialTheme.typography.bodySmall)
                                }
                                
                                if (item.status == "completed" && item.task_id != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { 
                                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(apiClient.getDownloadUrl(item.task_id)))
                                            context.startActivity(i)
                                        }) {
                                            Icon(Icons.Default.Download, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Download")
                                        }
                                        Button(onClick = { 
                                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(apiClient.getStreamUrl(item.task_id)))
                                            context.startActivity(i)
                                        }) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Stream")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}