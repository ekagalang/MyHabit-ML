package com.habittracker.ml.ui.habits

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.habittracker.ml.R
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.utils.HabitReminderScheduler
import com.habittracker.ml.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

class AddEditHabitFragment : Fragment() {

    private lateinit var viewModel: HabitsViewModel
    private lateinit var toolbar: MaterialToolbar
    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var sliderFrequency: Slider
    private lateinit var textViewFrequencyValue: TextView
    private lateinit var switchReminder: SwitchMaterial
    private lateinit var textViewReminderTimeLabel: TextView
    private lateinit var buttonPickTime: MaterialButton
    private lateinit var buttonSave: MaterialButton

    private lateinit var appPreferences: AppPreferences
    private var selectedReminderTime: String = "08:00"

    private val categories = listOf(
        "Health",
        "Productivity",
        "Learning",
        "Mindfulness",
        "Social"
    )

    private val categoryIcons = mapOf(
        "Health" to "💪",
        "Productivity" to "🎯",
        "Learning" to "📚",
        "Mindfulness" to "🧘",
        "Social" to "👥"
    )

    private val categoryColors = mapOf(
        "Health" to "#10B981",
        "Productivity" to "#6366F1",
        "Learning" to "#F59E0B",
        "Mindfulness" to "#8B5CF6",
        "Social" to "#EC4899"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar)
        editTextName = view.findViewById(R.id.editTextName)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        sliderFrequency = view.findViewById(R.id.sliderFrequency)
        textViewFrequencyValue = view.findViewById(R.id.textViewFrequencyValue)
        switchReminder = view.findViewById(R.id.switchReminder)
        textViewReminderTimeLabel = view.findViewById(R.id.textViewReminderTimeLabel)
        buttonPickTime = view.findViewById(R.id.buttonPickTime)
        buttonSave = view.findViewById(R.id.buttonSave)

        // Setup preferences
        appPreferences = AppPreferences(requireContext())
        selectedReminderTime = appPreferences.defaultReminderTime

        // Setup ViewModel
        val database = HabitDatabase.getDatabase(requireContext())
        val repository = HabitRepository(database.habitDao(), database.checkInDao())
        val factory = HabitsViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory)[HabitsViewModel::class.java]

        // Create notification channel
        NotificationHelper.createNotificationChannel(requireContext())

        // Setup toolbar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Setup category spinner
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        // Setup frequency slider
        sliderFrequency.addOnChangeListener { _, value, _ ->
            val times = value.toInt()
            textViewFrequencyValue.text = "$times times"
        }

        // Setup reminder switch
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textViewReminderTimeLabel.visibility = View.VISIBLE
                buttonPickTime.visibility = View.VISIBLE
            } else {
                textViewReminderTimeLabel.visibility = View.GONE
                buttonPickTime.visibility = View.GONE
            }
        }

        // Setup time picker button
        updateTimeButtonText(selectedReminderTime)
        buttonPickTime.setOnClickListener {
            showTimePicker()
        }

        // Save button click
        buttonSave.setOnClickListener {
            saveHabit()
        }
    }

    private fun showTimePicker() {
        val parts = selectedReminderTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                selectedReminderTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                updateTimeButtonText(selectedReminderTime)
            },
            hour,
            minute,
            false // 12-hour format
        )
        timePickerDialog.show()
    }

    private fun updateTimeButtonText(time: String) {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        buttonPickTime.text = timeFormat.format(calendar.time)
    }

    private fun saveHabit() {
        val name = editTextName.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val frequency = sliderFrequency.value.toInt()
        val reminderEnabled = switchReminder.isChecked

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter habit name", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter description", Toast.LENGTH_SHORT).show()
            return
        }

        // Create habit
        val habit = Habit(
            name = name,
            description = description,
            category = category.lowercase(),
            targetFrequency = frequency,
            reminderEnabled = reminderEnabled,
            reminderTime = if (reminderEnabled) selectedReminderTime else null,
            icon = categoryIcons[category] ?: "🎯",
            color = categoryColors[category] ?: "#6366F1"
        )

        // Save to database with callback
        viewModel.insertHabit(habit) { habitId ->
            Toast.makeText(requireContext(), "Habit created! 🎉", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}