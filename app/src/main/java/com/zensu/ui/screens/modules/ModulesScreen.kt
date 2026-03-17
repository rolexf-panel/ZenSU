package com.zensu.ui.screens.modules

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.zensu.model.Module
import com.zensu.ui.components.EmptyCard
import com.zensu.ui.components.LoadingCard
import com.zensu.viewmodel.ModulesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    viewModel: ModulesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val modules by viewModel.modules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showInstallDialog by remember { mutableStateOf(false) }
    var installResult by remember { mutableStateOf<String?>(null) }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            installResult = "Installing from: ${it.lastPathSegment}..."
            viewModel.installModule(context, it)
        }
    }
    
    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text("Install Module") },
            text = {
                Column {
                    Text("Install module from ZIP file:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            showInstallDialog = false
                            filePickerLauncher.launch("application/zip")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select ZIP File")
                    }
                    installResult?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstallDialog = false }) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Modules",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showInstallDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Install Module")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search modules...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            isLoading -> {
                LoadingCard()
            }
            modules.isEmpty() -> {
                EmptyCard(message = "No modules found")
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(modules, key = { it.id }) { module ->
                        ModuleItem(
                            module = module,
                            onToggle = { viewModel.toggleModule(module) },
                            onOpenWebUI = { 
                                if (module.hasWebUI) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(module.webUIUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleItem(
    module: Module,
    onToggle: (Boolean) -> Unit,
    onOpenWebUI: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    
    if (showDetails) {
        Dialog(onDismissRequest = { showDetails = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        module.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Version: ${module.version}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Author: ${module.author}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Description:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        module.description.ifEmpty { "No description" },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (module.hasWebUI) {
                            Button(
                                onClick = onOpenWebUI,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WebUI")
                            }
                        }
                        OutlinedButton(
                            onClick = { showDetails = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        module.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (module.hasWebUI) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AssistChip(
                            onClick = onOpenWebUI,
                            label = { Text("WebUI", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "v${module.version} by ${module.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (module.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        module.description.take(80) + if (module.description.length > 80) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { showDetails = true }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = module.enabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}
