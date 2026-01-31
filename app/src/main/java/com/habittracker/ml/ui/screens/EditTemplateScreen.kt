package com.habittracker.ml.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.HabitTemplate
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.ui.habits.TemplatesViewModel
import com.habittracker.ml.ui.habits.TemplatesViewModelFactory
import com.habittracker.ml.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateScreen(
    templateId: Int?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("⭐") }
    var defaultFrequency by remember { mutableStateOf("daily") }
    var tags by remember { mutableStateOf("") }
    var existingTemplate by remember { mutableStateOf<HabitTemplate?>(null) }

    LaunchedEffect(templateId) {
        if (templateId != null) {
            val template = repository.getTemplateById(templateId)
            existingTemplate = template
            template?.let {
                name = it.name
                description = it.description
                category = it.category
                icon = it.icon
                defaultFrequency = it.defaultFrequency
                tags = it.tags
            }
        }
    }

    fun saveTemplate() {
        if (name.isBlank() || category.isBlank()) {
            Toast.makeText(context, "Name and Category are required", Toast.LENGTH_SHORT).show()
            return
        }

        val newTemplate = HabitTemplate(
            id = templateId ?: 0,
            name = name,
            description = description,
            category = category,
            icon = icon,
            defaultFrequency = defaultFrequency,
            tags = tags
        )

        if (templateId == null) {
            viewModel.insertTemplate(newTemplate) { onNavigateBack() }
        } else {
            viewModel.updateTemplate(newTemplate) { onNavigateBack() }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BackgroundLight,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMain,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = if (templateId == null) "Create Template" else "Edit Template",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (templateId != null) {
                            IconButton(onClick = {
                                existingTemplate?.let {
                                    viewModel.deleteTemplate(it)
                                    onNavigateBack()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = AccentError
                                )
                            }
                        }

                        IconButton(onClick = { saveTemplate() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = Primary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FormCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Basics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }

                FormCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Category & Icon",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = icon,
                            onValueChange = { icon = it },
                            label = { Text("Icon (Emoji)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                FormCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Frequency:", color = TextMuted, modifier = Modifier.padding(end = 16.dp))
                            RadioButton(
                                selected = defaultFrequency == "daily",
                                onClick = { defaultFrequency = "daily" }
                            )
                            Text("Daily", color = TextMain)
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = defaultFrequency == "weekly",
                                onClick = { defaultFrequency = "weekly" }
                            )
                            Text("Weekly", color = TextMain)
                        }
                        HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 12.dp))
                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        content()
    }
}
