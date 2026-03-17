package com.zensu.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zensu.ui.theme.ThemeManager
import androidx.compose.foundation.isSystemInDarkTheme

data class IconOption(
    val name: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zensu_prefs", 0) }
    
    var useSystemDarkMode by remember { mutableStateOf(!prefs.getBoolean("manual_dark_mode_set", false)) }
    var darkModeOverride by remember { mutableStateOf(prefs.getBoolean("dark_mode_value", false)) }
    
    val systemDarkMode = isSystemInDarkTheme()
    val isDarkMode = if (useSystemDarkMode) systemDarkMode else darkModeOverride
    
    // Update ThemeManager when settings change
    LaunchedEffect(useSystemDarkMode, darkModeOverride) {
        ThemeManager.setDarkMode(useSystemDarkMode, darkModeOverride)
        prefs.edit()
            .putBoolean("manual_dark_mode_set", !useSystemDarkMode)
            .putBoolean("dark_mode_value", darkModeOverride)
            .apply()
    }
    
    val presetIcons = listOf(
        IconOption("ZenSU", Icons.Default.Shield, Color(0xFF6750A4)),
        IconOption("KernelSU", Icons.Default.Security, Color(0xFF2196F3)),
        IconOption("Magisk", Icons.Default.Star, Color(0xFFFF9800)),
        IconOption("Dark", Icons.Default.DarkMode, Color(0xFF424242)),
        IconOption("Green", Icons.Default.CheckCircle, Color(0xFF4CAF50)),
        IconOption("Red", Icons.Default.Favorite, Color(0xFFF44336)),
        IconOption("Purple", Icons.Default.Palette, Color(0xFF9C27B0)),
        IconOption("Cyan", Icons.Default.Water, Color(0xFF00BCD4))
    )
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            prefs.edit().putString("custom_icon", it.toString()).apply()
        }
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
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DarkMode, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (isDarkMode) "Dark (System: ${if (systemDarkMode) "Dark" else "Light"})" 
                            else "Light (System: ${if (systemDarkMode) "Dark" else "Light"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useSystemDarkMode,
                        onCheckedChange = { 
                            useSystemDarkMode = it
                            ThemeManager.setDarkMode(it, darkModeOverride)
                        }
                    )
                }
                
                if (!useSystemDarkMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Manual:", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = darkModeOverride,
                            onCheckedChange = { 
                                darkModeOverride = it
                                ThemeManager.setDarkMode(useSystemDarkMode, it)
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "App Icon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Choose an icon:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(presetIcons) { iconOption ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                prefs.edit()
                                    .putString("selected_icon", iconOption.name)
                                    .remove("custom_icon")
                                    .apply()
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(iconOption.backgroundColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    iconOption.icon,
                                    contentDescription = iconOption.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(iconOption.name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Custom",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Custom", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = {
                    prefs.edit()
                        .remove("selected_icon")
                        .remove("custom_icon")
                        .apply()
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset to Default")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Font Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                var selectedFont by remember { mutableStateOf(prefs.getString("selected_font", "System Default") ?: "System Default") }
                val fontOptions = listOf("System Default", "Sans Serif", "Serif", "Monospace")
                
                fontOptions.forEach { font ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFont == font,
                            onClick = { 
                                selectedFont = font
                                prefs.edit().putString("selected_font", font).apply()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = font,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = when (font) {
                                "Sans Serif" -> FontFamily.SansSerif
                                "Serif" -> FontFamily.Serif
                                "Monospace" -> FontFamily.Monospace
                                else -> FontFamily.Default
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                            "Version 1.0.5",
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
