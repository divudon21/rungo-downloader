package com.agon.app

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.agon.app.data.ThemeMode
import com.agon.app.ui.screens.HomeScreen
import com.agon.app.ui.screens.HistoryScreen
import com.agon.app.ui.screens.SettingsScreen
import com.agon.app.ui.screens.UploadScreen
import com.agon.app.ui.screens.CloudTransferScreen
import com.agon.app.ui.theme.AgonAppTheme
import com.agon.app.viewmodel.SettingsViewModel

import com.agon.app.ui.screens.CloudTransferScreen

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val amoledMode by settingsViewModel.amoledMode.collectAsState()
            
            val isDark = when(themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            AgonAppTheme(darkTheme = isDark, amoledMode = amoledMode) {
                val navController = rememberNavController()
                val items = listOf(
                    Screen.Home,
                    Screen.Upload,
                    Screen.History,
                    Screen.CloudTransfer,
                    Screen.Settings
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            when(screen) {
                                                Screen.Home -> Icons.Filled.Home
                                                Screen.Upload -> Icons.Default.UploadFile
                                                Screen.History -> Icons.Filled.History
                                                Screen.CloudTransfer -> Icons.Filled.CloudUpload
                                                Screen.Settings -> Icons.Filled.Settings
                                            },
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text(screen.route, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) { HomeScreen() }
                        composable(Screen.Upload.route) { UploadScreen() }
                        composable(Screen.History.route) { HistoryScreen() }
                        composable(Screen.CloudTransfer.route) { CloudTransferScreen() }
                        composable(Screen.Settings.route) { SettingsScreen() }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("Home")
    object Upload : Screen("Upload")
    object History : Screen("History")
    object CloudTransfer : Screen("Transfer")
    object Settings : Screen("Settings")
}