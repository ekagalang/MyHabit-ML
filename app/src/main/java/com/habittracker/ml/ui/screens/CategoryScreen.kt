package com.habittracker.ml.ui.screens

import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.data.local.preferences.CustomCategory
import com.habittracker.ml.ui.theme.*

@Composable
fun CategoryScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }

    var categoriesData by remember { mutableStateOf(preferences.getCategories()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CustomCategory?>(null) }
    val visibleCategories = remember(categoriesData) { categoriesData.filter { it.isEnabled } }

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
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Category",
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
            if (visibleCategories.isEmpty()) {
                item {
                    Text(
                        text = "No categories yet.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(visibleCategories.size) { index ->
                    val category = visibleCategories[index]
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
                                    color = Color(AndroidColor.parseColor(category.colorHex)),
                                    modifier = Modifier.size(18.dp)
                                ) {}
                                Text(
                                    text = category.name,
                                    color = TextMain,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { editingCategory = category }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val updated = if (category.isDefault) {
                                            categoriesData.map {
                                                if (it.id == category.id) it.copy(isEnabled = false) else it
                                            }
                                        } else {
                                            categoriesData.filterNot { it.id == category.id }
                                        }
                                        categoriesData = updated
                                        preferences.setCategories(updated)
                                    }
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
        val colorOptions = listOf(
            "#10B981", "#6366F1", "#F59E0B", "#EF4444",
            "#8B5CF6", "#0EA5E9", "#14B8A6", "#EC4899"
        )
        var newCategoryName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(colorOptions.first()) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pick a color", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colorOptions.forEach { hex ->
                            val isSelected = hex == selectedColor
                            Surface(
                                shape = CircleShape,
                                color = Color(AndroidColor.parseColor(hex)),
                                modifier = Modifier
                                    .size(if (isSelected) 26.dp else 22.dp)
                                    .clickable { selectedColor = hex }
                            ) {}
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newCategoryName.trim()
                        if (name.isEmpty()) {
                            Toast.makeText(context, "Name can't be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val existingIds = categoriesData.map { it.id }.toSet()
                        val baseId = slugifyCategory(name)
                        var newId = baseId
                        var counter = 1
                        while (existingIds.contains(newId)) {
                            counter += 1
                            newId = "${baseId}_$counter"
                        }

                        val newCategory = CustomCategory(
                            id = newId,
                            name = name,
                            colorHex = selectedColor,
                            isDefault = false,
                            isEnabled = true
                        )
                        val updated = categoriesData + newCategory
                        categoriesData = updated
                        preferences.setCategories(updated)
                        showAddDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (editingCategory != null) {
        val categoryToEdit = editingCategory!!
        val colorOptions = listOf(
            "#10B981", "#6366F1", "#F59E0B", "#EF4444",
            "#8B5CF6", "#0EA5E9", "#14B8A6", "#EC4899"
        )
        var editName by remember(categoryToEdit.id) { mutableStateOf(categoryToEdit.name) }
        var selectedColor by remember(categoryToEdit.id) { mutableStateOf(categoryToEdit.colorHex) }

        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Edit Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Category name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pick a color", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colorOptions.forEach { hex ->
                            val isSelected = hex == selectedColor
                            Surface(
                                shape = CircleShape,
                                color = Color(AndroidColor.parseColor(hex)),
                                modifier = Modifier
                                    .size(if (isSelected) 26.dp else 22.dp)
                                    .clickable { selectedColor = hex }
                            ) {}
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = editName.trim()
                        if (name.isEmpty()) {
                            Toast.makeText(context, "Name can't be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val updated = categoriesData.map {
                            if (it.id == categoryToEdit.id) {
                                it.copy(name = name, colorHex = selectedColor, isEnabled = true)
                            } else {
                                it
                            }
                        }
                        categoriesData = updated
                        preferences.setCategories(updated)
                        editingCategory = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) { Text("Cancel") }
            }
        )
    }
}
