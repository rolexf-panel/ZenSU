package com.zensu.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zensu.ui.theme.ThemeManager

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zensu_prefs", 0) }
    
    val systemDarkMode = isSystemInDarkTheme()
    var useDarkMode by remember { mutableStateOf(!prefs.getBoolean("dark_mode_system", true)) }
    var versionClickCount by remember { mutableStateOf(0) }
    var showDeveloperOptions by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    val isDarkMode = if (useDarkMode) systemDarkMode else !systemDarkMode
    
    LaunchedEffect(useDarkMode) {
        ThemeManager.setDarkMode(useDarkMode, !systemDarkMode)
        prefs.edit().putBoolean("dark_mode_system", useDarkMode).apply()
    }
    
    // Language options
    val languages = listOf(
        "en" to "English",
        "id" to "Bahasa Indonesia",
        "zh" to "中文",
        "es" to "Español",
        "pt" to "Português",
        "ru" to "Русский",
        "ja" to "日本語",
        "ko" to "한국어",
        "ar" to "العربية",
        "tr" to "Türkçe"
    )
    
    var selectedLanguage by remember { mutableStateOf(prefs.getString("language", "en") ?: "en") }
    
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = code
                                    prefs.edit().putString("language", code).apply()
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == code,
                                onClick = {
                                    selectedLanguage = code
                                    prefs.edit().putString("language", code).apply()
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    if (showDeveloperOptions) {
        AlertDialog(
            onDismissRequest = { showDeveloperOptions = false },
            title = { Text("Developer Options") },
            text = {
                Column {
                    DeveloperOption(
                        "Clear Module Cache",
                        "Remove cached module data"
                    ) {
                        Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                    }
                    DeveloperOption(
                        "Reload Modules",
                        "Force reload modules list"
                    ) {
                        Toast.makeText(context, "Modules reloaded", Toast.LENGTH_SHORT).show()
                    }
                    DeveloperOption(
                        "View Root Logs",
                        "Show root access logs"
                    ) {
                        Toast.makeText(context, "Opening logs...", Toast.LENGTH_SHORT).show()
                    }
                    DeveloperOption(
                        "Test Notification",
                        "Send test notification"
                    ) {
                        Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeveloperOptions = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Appearance Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Dark Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DarkMode, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (isDarkMode) "Dark" else "Light",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useDarkMode,
                        onCheckedChange = { useDarkMode = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Language
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Language", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            languages.find { it.first == selectedLanguage }?.second ?: "English",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // About Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .clickable {
                        versionClickCount++
                        if (versionClickCount >= 5) {
                            showDeveloperOptions = true
                            versionClickCount = 0
                            Toast.makeText(context, "Developer Options Opened!", Toast.LENGTH_SHORT).show()
                        } else {
                            val remaining = 5 - versionClickCount
                            Toast.makeText(context, "$remaining more clicks for developer options", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(16.dp)
            ) {
                Text(
                    "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ZenSU", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Version 1.0.7",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "A highly customizable and user-friendly root manager for Android.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DeveloperOption(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}
