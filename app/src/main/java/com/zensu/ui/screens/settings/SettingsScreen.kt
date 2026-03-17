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
import androidx.compose.foundation.isSystemInDarkTheme

data class IconOption(
    val name: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val systemDarkMode = isSystemInDarkTheme()
    var useSystemDarkMode by remember { mutableStateOf(true) }
    var darkModeOverride by remember { mutableStateOf(systemDarkMode) }
    var selectedIconUri by remember { mutableStateOf<Uri?>(null) }
    
    val isDarkMode = if (useSystemDarkMode) systemDarkMode else darkModeOverride
    
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
            selectedIconUri = it
            context.getSharedPreferences("zensu_prefs", 0)
                .edit()
                .putString("custom_icon", it.toString())
                .apply()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Appearance",
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
                            if (isDarkMode) "Following System (Dark)" else "Following System (Light)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useSystemDarkMode,
                        onCheckedChange = { useSystemDarkMode = it }
                    )
                }
                
                if (!useSystemDarkMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text("Manual: ", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = darkModeOverride,
                            onCheckedChange = { darkModeOverride = it }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "App Icon",
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
                                context.getSharedPreferences("zensu_prefs", 0)
                                    .edit()
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
                    context.getSharedPreferences("zensu_prefs", 0)
                        .edit()
                        .remove("selected_icon")
                        .remove("custom_icon")
                        .apply()
                    selectedIconUri = null
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
                    text = "Font Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                var selectedFont by remember { mutableStateOf("System Default") }
                val fontOptions = listOf("System Default", "Sans Serif", "Serif", "Monospace")
                
                fontOptions.forEach { font ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFont == font,
                            onClick = { selectedFont = font }
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
                    text = "About",
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
                            "Version 1.0.4",
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
