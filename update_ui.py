import os
import re

# Update MainActivity.kt
with open("app/src/main/java/com/agon/app/MainActivity.kt", "r") as f:
    main_content = f.read()

main_content = main_content.replace("import androidx.compose.material.icons.filled.UploadFile", "import androidx.compose.material.icons.filled.UploadFile\nimport androidx.compose.material.icons.filled.CloudUpload")
main_content = main_content.replace("import com.agon.app.ui.screens.SettingsScreen", "import com.agon.app.ui.screens.SettingsScreen\nimport com.agon.app.ui.screens.CloudTransferScreen")

main_content = main_content.replace("Screen.History,", "Screen.History,\n                    Screen.CloudTransfer,")
main_content = main_content.replace("Screen.History -> Icons.Filled.History", "Screen.History -> Icons.Filled.History\n                                                Screen.CloudTransfer -> Icons.Filled.CloudUpload")
main_content = main_content.replace("composable(Screen.History.route) { HistoryScreen() }", "composable(Screen.History.route) { HistoryScreen() }\n                        composable(Screen.CloudTransfer.route) { CloudTransferScreen() }")
main_content = main_content.replace('object History : Screen("History")', 'object History : Screen("History")\n    object CloudTransfer : Screen("Cloud Transfer")')

with open("app/src/main/java/com/agon/app/MainActivity.kt", "w") as f:
    f.write(main_content)

# Update HomeScreen.kt
with open("app/src/main/java/com/agon/app/ui/screens/HomeScreen.kt", "r") as f:
    home_content = f.read()

home_content = home_content.replace("import androidx.compose.material.icons.filled.PlayArrow", "import androidx.compose.material.icons.filled.PlayArrow\nimport androidx.compose.material.icons.filled.Share")

share_util = """fun shareLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(Intent.createChooser(intent, "Share Link"))
}

fun formatSize"""
home_content = home_content.replace("fun formatSize", share_util)

# Fix Download Link UI
old_dl_ui = """                            Row(
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
                            }"""
new_dl_ui = """                            Row(
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
                            }"""
home_content = home_content.replace(old_dl_ui, new_dl_ui)

# Fix Stream Link UI
old_st_ui = """                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = { copyToClipboard(context, streamUrl, "Stream Link") }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy")
                                }
                                Button(onClick = { 
                                    openStreamInPlayer(context, streamUrl, status.original_filename)
                                }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open")
                                }
                            }"""
new_st_ui = """                            Row(
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
                            }"""
home_content = home_content.replace(old_st_ui, new_st_ui)

with open("app/src/main/java/com/agon/app/ui/screens/HomeScreen.kt", "w") as f:
    f.write(home_content)

# Update UploadScreen.kt
with open("app/src/main/java/com/agon/app/ui/screens/UploadScreen.kt", "r") as f:
    up_content = f.read()

up_content = up_content.replace("import androidx.compose.material.icons.filled.UploadFile", "import androidx.compose.material.icons.filled.UploadFile\nimport androidx.compose.material.icons.filled.Share")

old_up_dl = """                            Row(
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
                            }"""
up_content = up_content.replace(old_up_dl, new_dl_ui)

old_up_st = """                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = { copyToClipboard(context, streamUrl, "Stream Link") }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Copy")
                                }
                                Button(onClick = { 
                                    openStreamInPlayer(context, streamUrl, "video.mp4") // Defaulting to video to trigger player
                                }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open")
                                }
                            }"""
new_up_st = new_st_ui.replace("status.original_filename", '"video.mp4"')
up_content = up_content.replace(old_up_st, new_up_st)

with open("app/src/main/java/com/agon/app/ui/screens/UploadScreen.kt", "w") as f:
    f.write(up_content)

# Update HistoryScreen.kt
with open("app/src/main/java/com/agon/app/ui/screens/HistoryScreen.kt", "r") as f:
    hist_content = f.read()

hist_content = hist_content.replace("import androidx.compose.material.icons.filled.PlayArrow", "import androidx.compose.material.icons.filled.PlayArrow\nimport androidx.compose.material.icons.filled.Share\nimport androidx.compose.material.icons.filled.ContentCopy")

old_hist_btns = """                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { 
                                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(apiClient.getDownloadUrl(item.task_id)))
                                            context.startActivity(i)
                                        }) {
                                            Icon(Icons.Default.Download, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Download")
                                        }
                                        Button(onClick = { 
                                            openStreamInPlayer(context, apiClient.getStreamUrl(item.task_id), item.original_filename)
                                        }) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Stream")
                                        }
                                    }"""
new_hist_btns = """                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                        Button(onClick = { 
                                            copyToClipboard(context, apiClient.getDownloadUrl(item.task_id), "Download Link")
                                        }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text("Copy", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                                        }
                                        Button(onClick = { 
                                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(apiClient.getDownloadUrl(item.task_id)))
                                            context.startActivity(i)
                                        }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text("DL", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                                        }
                                        Button(onClick = { 
                                            openStreamInPlayer(context, apiClient.getStreamUrl(item.task_id), item.original_filename)
                                        }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text("Play", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                                        }
                                        Button(onClick = { 
                                            shareLink(context, apiClient.getDownloadUrl(item.task_id))
                                        }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text("Share", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }"""
hist_content = hist_content.replace(old_hist_btns, new_hist_btns)

with open("app/src/main/java/com/agon/app/ui/screens/HistoryScreen.kt", "w") as f:
    f.write(hist_content)
