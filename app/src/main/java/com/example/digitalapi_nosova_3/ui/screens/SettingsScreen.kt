package com.example.digitalapi_nosova_3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    theme: String,
    autoSync: Boolean,
    cacheTtlDays: Int,
    onThemeChange: (String) -> Unit,
    onAutoSyncChange: (Boolean) -> Unit,
    onCacheTtlChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTtlDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showThemeDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Theme")
                    Text(
                        when (theme) {
                            "light" -> "Light"
                            "dark" -> "Dark"
                            else -> "System"
                        },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Divider()

            Text("Data", style = MaterialTheme.typography.titleMedium)

            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Sync")
                        Text(
                            "Update cache every 6 hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoSync,
                        onCheckedChange = onAutoSyncChange
                    )
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showTtlDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cache TTL")
                    Text("$cacheTtlDays days", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    RadioButtonOption("System", theme == "system") { onThemeChange("system"); showThemeDialog = false }
                    RadioButtonOption("Light", theme == "light") { onThemeChange("light"); showThemeDialog = false }
                    RadioButtonOption("Dark", theme == "dark") { onThemeChange("dark"); showThemeDialog = false }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showTtlDialog) {
        AlertDialog(
            onDismissRequest = { showTtlDialog = false },
            title = { Text("Cache TTL (days)") },
            text = {
                Column {
                    RadioButtonOption("1 day", cacheTtlDays == 1) { onCacheTtlChange(1); showTtlDialog = false }
                    RadioButtonOption("3 days", cacheTtlDays == 3) { onCacheTtlChange(3); showTtlDialog = false }
                    RadioButtonOption("7 days", cacheTtlDays == 7) { onCacheTtlChange(7); showTtlDialog = false }
                    RadioButtonOption("14 days", cacheTtlDays == 14) { onCacheTtlChange(14); showTtlDialog = false }
                    RadioButtonOption("30 days", cacheTtlDays == 30) { onCacheTtlChange(30); showTtlDialog = false }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTtlDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun RadioButtonOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.weight(1f))
    }
}
