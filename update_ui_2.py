import os

with open("app/src/main/java/com/agon/app/MainActivity.kt", "r") as f:
    main_content = f.read()

main_content = main_content.replace("import androidx.compose.material.icons.filled.UploadFile", "import androidx.compose.material.icons.filled.UploadFile\nimport androidx.compose.material.icons.filled.CloudUpload")
main_content = main_content.replace("import com.agon.app.ui.screens.SettingsScreen", "import com.agon.app.ui.screens.SettingsScreen\nimport com.agon.app.ui.screens.CloudTransferScreen")

if "Screen.CloudTransfer," not in main_content:
    main_content = main_content.replace("Screen.History,", "Screen.History,\n                    Screen.CloudTransfer,")
    main_content = main_content.replace("Screen.History -> Icons.Filled.History", "Screen.History -> Icons.Filled.History\n                                                Screen.CloudTransfer -> Icons.Filled.CloudUpload")
    main_content = main_content.replace("composable(Screen.History.route) { HistoryScreen() }", "composable(Screen.History.route) { HistoryScreen() }\n                        composable(Screen.CloudTransfer.route) { CloudTransferScreen() }")
    main_content = main_content.replace('object History : Screen("History")', 'object History : Screen("History")\n    object CloudTransfer : Screen("Cloud Transfer")')

with open("app/src/main/java/com/agon/app/MainActivity.kt", "w") as f:
    f.write(main_content)
