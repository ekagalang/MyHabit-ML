package com.habittracker.ml.ui.habits

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.ml.data.local.entities.HabitTemplate
import com.habittracker.ml.data.repository.HabitRepository
import kotlinx.coroutines.launch

class TemplatesViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    val allTemplates: LiveData<List<HabitTemplate>> = repository.getAllTemplates()

    fun insertTemplate(template: HabitTemplate, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertTemplate(template)
            onComplete?.invoke()
        }
    }

    fun updateTemplate(template: HabitTemplate, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.updateTemplate(template)
            onComplete?.invoke()
        }
    }

    fun deleteTemplate(template: HabitTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }
}

class TemplatesViewModelFactory(
    private val repository: HabitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TemplatesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TemplatesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
