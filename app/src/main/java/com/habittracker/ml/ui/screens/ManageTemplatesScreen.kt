package com.habittracker.ml.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.HabitTemplate
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.ui.habits.TemplatesViewModel
import com.habittracker.ml.ui.habits.TemplatesViewModelFactory
import com.habittracker.ml.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTemplatesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditTemplate: (Int?) -> Unit
) {
    val context = LocalContext.current
    val database = HabitDatabase.getDatabase(context)
    val repository = remember {
        HabitRepository(
            database.habitDao(),
            database.checkInDao(),
            database.habitTemplateDao()
        )
    }
    val viewModel: TemplatesViewModel = viewModel(
        factory = TemplatesViewModelFactory(repository)
    )

    val templates by viewModel.allTemplates.observeAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<HabitTemplate?>(null) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Surface(color = BackgroundLight, shadowElevation = 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMain
                        )
                    }

                    Text(
                        text = "Manage Templates",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Template",
                            tint = TextMain
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (templates.isEmpty()) {
                item {
                    Text(
                        text = "No templates yet.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(templates) { template ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceLight,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = getTemplateCategoryColor(template.category),
                                    modifier = Modifier.size(18.dp)
                                ) {}

                                Text(
                                    text = "${template.icon} ${template.name}",
                                    color = TextMain,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { editingTemplate = template }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteTemplate(template) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = AccentError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("⭐") }
        var defaultFrequency by remember { mutableStateOf("daily") }
        var tags by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Template") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it },
                        label = { Text("Icon (Emoji)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Frequency:", color = TextMuted, modifier = Modifier.padding(end = 12.dp))
                        RadioButton(
                            selected = defaultFrequency == "daily",
                            onClick = { defaultFrequency = "daily" }
                        )
                        Text("Daily", color = TextMain)
                        Spacer(Modifier.width(12.dp))
                        RadioButton(
                            selected = defaultFrequency == "weekly",
                            onClick = { defaultFrequency = "weekly" }
                        )
                        Text("Weekly", color = TextMain)
                    }
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedCategory = category.trim()
                        if (trimmedName.isEmpty() || trimmedCategory.isEmpty()) {
                            Toast.makeText(context, "Name and Category are required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val newTemplate = HabitTemplate(
                            id = 0,
                            name = trimmedName,
                            description = description.trim(),
                            category = trimmedCategory,
                            icon = icon.trim().ifEmpty { "⭐" },
                            defaultFrequency = defaultFrequency,
                            tags = tags.trim()
                        )
                        viewModel.insertTemplate(newTemplate)
                        showAddDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (editingTemplate != null) {
        val templateToEdit = editingTemplate!!
        var name by remember(templateToEdit.id) { mutableStateOf(templateToEdit.name) }
        var description by remember(templateToEdit.id) { mutableStateOf(templateToEdit.description) }
        var category by remember(templateToEdit.id) { mutableStateOf(templateToEdit.category) }
        var icon by remember(templateToEdit.id) { mutableStateOf(templateToEdit.icon) }
        var defaultFrequency by remember(templateToEdit.id) { mutableStateOf(templateToEdit.defaultFrequency) }
        var tags by remember(templateToEdit.id) { mutableStateOf(templateToEdit.tags) }

        AlertDialog(
            onDismissRequest = { editingTemplate = null },
            title = { Text("Edit Template") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it },
                        label = { Text("Icon (Emoji)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Frequency:", color = TextMuted, modifier = Modifier.padding(end = 12.dp))
                        RadioButton(
                            selected = defaultFrequency == "daily",
                            onClick = { defaultFrequency = "daily" }
                        )
                        Text("Daily", color = TextMain)
                        Spacer(Modifier.width(12.dp))
                        RadioButton(
                            selected = defaultFrequency == "weekly",
                            onClick = { defaultFrequency = "weekly" }
                        )
                        Text("Weekly", color = TextMain)
                    }
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedCategory = category.trim()
                        if (trimmedName.isEmpty() || trimmedCategory.isEmpty()) {
                            Toast.makeText(context, "Name and Category are required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val updated = templateToEdit.copy(
                            name = trimmedName,
                            description = description.trim(),
                            category = trimmedCategory,
                            icon = icon.trim().ifEmpty { "⭐" },
                            defaultFrequency = defaultFrequency,
                            tags = tags.trim()
                        )
                        viewModel.updateTemplate(updated)
                        editingTemplate = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingTemplate = null }) { Text("Cancel") }
            }
        )
    }
}

private fun getTemplateCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "health" -> CategoryHealth
        "productivity" -> CategoryProductivity
        "learning" -> CategoryLearning
        "mindfulness" -> CategoryMindfulness
        "social" -> CategorySocial
        else -> Primary
    }
}
