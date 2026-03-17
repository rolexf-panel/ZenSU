package com.zensu.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zensu.ui.navigation.Screen
import com.zensu.ui.navigation.bottomNavItems
import com.zensu.ui.screens.home.HomeScreen
import com.zensu.ui.screens.modules.ModulesScreen
import com.zensu.ui.screens.settings.SettingsScreen
import com.zensu.ui.theme.ThemeManager
import com.zensu.ui.theme.ZenSUTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved theme preferences
        val prefs = getSharedPreferences("zensu_prefs", 0)
        val manualDarkModeSet = prefs.getBoolean("manual_dark_mode_set", false)
        val darkModeValue = prefs.getBoolean("dark_mode_value", false)
        ThemeManager.setDarkMode(!manualDarkModeSet, darkModeValue)
        
        enableEdgeToEdge()
        setContent {
            ZenSUTheme {
                var showPowerMenu by remember { mutableStateOf(false) }
                val navController = rememberNavController()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("ZenSU") },
                            actions = {
                                Box {
                                    IconButton(onClick = { showPowerMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.PowerSettingsNew,
                                            contentDescription = "Power"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showPowerMenu,
                                        onDismissRequest = { showPowerMenu = false }
                                    ) {
                                        PowerMenuItems(
                                            onDismiss = { showPowerMenu = false }
                                        )
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            
                            bottomNavItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                                                screen.selectedIcon
                                            } else {
                                                screen.unselectedIcon
                                            },
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title) },
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
                        composable(Screen.Home.route) {
                            HomeScreen()
                        }
                        composable(Screen.Modules.route) {
                            ModulesScreen()
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerMenuItems(onDismiss: () -> Unit) {
    val rebootOptions = listOf(
        "Reboot" to "reboot",
        "Recovery" to "reboot recovery",
        "Bootloader" to "reboot bootloader",
        "Fastboot" to "reboot fastboot",
        "EDL" to "reboot edl",
        "Sideload" to "reboot sideload"
    )
    
    var showConfirmDialog by remember { mutableStateOf<String?>(null) }
    
    DropdownMenuItem(
        text = { Text("Power Menu") },
        onClick = { },
        leadingIcon = {
            Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
        }
    )
    
    HorizontalDivider()
    
    rebootOptions.forEach { (label, command) ->
        DropdownMenuItem(
            text = { Text(label) },
            onClick = {
                onDismiss()
                showConfirmDialog = "$label|$command"
            }
        )
    }
    
    if (showConfirmDialog != null) {
        val parts = showConfirmDialog!!.split("|")
        val label = parts[0]
        val command = parts[1]
        
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text("Confirm $label") },
            text = { Text("Are you sure you want to $label?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        com.zensu.util.PowerUtils.reboot(command)
                        showConfirmDialog = null
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
