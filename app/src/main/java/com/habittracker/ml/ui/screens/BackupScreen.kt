package com.habittracker.ml.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.ui.habits.HabitsViewModel
import com.habittracker.ml.ui.habits.HabitsViewModelFactory
import com.habittracker.ml.utils.DataExporter
import com.habittracker.ml.utils.DataImporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { HabitDatabase.getDatabase(context) }
    val repository = remember { HabitRepository(database.habitDao(), database.checkInDao(), database.habitTemplateDao()) }
    val viewModel: HabitsViewModel = viewModel(
        factory = HabitsViewModelFactory(repository)
    )

    val exporter = remember { DataExporter(repository) }
    val importer = remember { DataImporter(repository) }

    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importStrategy by remember { mutableStateOf(DataImporter.ImportStrategy.SKIP) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isImporting = true
                    val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()

                    if (content != null) {
                        val result = importer.importFromJson(content, importStrategy)

                        if (result.success) {
                                                            Toast.makeText(
                                                            context,
                                                            "Imported: ${result.habitsImported} habits, ${result.checkInsImported} check-ins",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {                            Toast.makeText(
                                context,
                                "Import failed: ${result.errors.firstOrNull()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isImporting = false
                    showImportDialog = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Upload, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Export Data",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        "Export your habits and check-ins to a file",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        isExporting = true
                                        val json = exporter.exportToJson()
                                        val file = exporter.saveToFile(context, json, "json")
                                        shareFile(context, file)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            },
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Description, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("JSON")
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        isExporting = true
                                        val csv = exporter.exportToCsv()
                                        val file = exporter.saveToFile(context, csv, "csv")
                                        shareFile(context, file)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            },
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.TableChart, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("CSV")
                        }
                    }

                    if (isExporting) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Import Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Import Data",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        "Restore your data from a backup file",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showImportDialog = true },
                        enabled = !isImporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Select Backup File")
                    }

                    if (isImporting) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Important Notes",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Text(
                        "• Backup files are saved to your device storage\n" +
                                "• JSON format preserves all data\n" +
                                "• CSV format is compatible with Excel\n" +
                                "• Importing will not delete existing data",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // Import Strategy Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Options") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("What should we do with existing habits?")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = importStrategy == DataImporter.ImportStrategy.SKIP,
                            onClick = { importStrategy = DataImporter.ImportStrategy.SKIP }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Skip", style = MaterialTheme.typography.bodyMedium)
                            Text("Keep existing habits", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = importStrategy == DataImporter.ImportStrategy.REPLACE,
                            onClick = { importStrategy = DataImporter.ImportStrategy.REPLACE }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Replace", style = MaterialTheme.typography.bodyMedium)
                            Text("Update existing habits", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { filePickerLauncher.launch("application/json") }
                ) {
                    Text("Select File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun shareFile(context: Context, file: java.io.File) {
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share Backup"))
}