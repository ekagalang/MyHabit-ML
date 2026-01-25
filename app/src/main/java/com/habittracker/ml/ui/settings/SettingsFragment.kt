package com.habittracker.ml.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.habittracker.ml.R
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.utils.ThemeManager
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar

    // Theme
    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var radioLight: MaterialRadioButton
    private lateinit var radioDark: MaterialRadioButton
    private lateinit var radioSystem: MaterialRadioButton
    private lateinit var textViewCurrentTheme: TextView

    // Notifications
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var buttonDefaultTime: MaterialButton

    // Daily Summary
    private lateinit var switchDailySummary: SwitchMaterial
    private lateinit var buttonSummaryTime: MaterialButton

    private lateinit var appPreferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar)

        // Theme
        radioGroupTheme = view.findViewById(R.id.radioGroupTheme)
        radioLight = view.findViewById(R.id.radioLight)
        radioDark = view.findViewById(R.id.radioDark)
        radioSystem = view.findViewById(R.id.radioSystem)
        textViewCurrentTheme = view.findViewById(R.id.textViewCurrentTheme)

        // Notifications
        switchNotifications = view.findViewById(R.id.switchNotifications)
        buttonDefaultTime = view.findViewById(R.id.buttonDefaultTime)

        // Daily Summary
        switchDailySummary = view.findViewById(R.id.switchDailySummary)
        buttonSummaryTime = view.findViewById(R.id.buttonSummaryTime)

        // Initialize preferences
        appPreferences = AppPreferences(requireContext())

        // Load saved preferences
        loadPreferences()

        // Toolbar navigation
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Theme selection
        setupThemeSelection()

        // Notification switch
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.notificationsEnabled = isChecked
        }

        // Default time picker
        buttonDefaultTime.setOnClickListener {
            showTimePicker(appPreferences.defaultReminderTime) { time ->
                appPreferences.defaultReminderTime = time
                updateTimeButton(buttonDefaultTime, time)
            }
        }

        // Daily summary switch
        switchDailySummary.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.dailySummaryEnabled = isChecked
        }

        // Summary time picker
        buttonSummaryTime.setOnClickListener {
            showTimePicker(appPreferences.dailySummaryTime) { time ->
                appPreferences.dailySummaryTime = time
                updateTimeButton(buttonSummaryTime, time)
            }
        }
    }

    private fun setupThemeSelection() {
        // Load current theme
        val currentMode = ThemeManager.getThemeMode(requireContext())
        when (currentMode) {
            ThemeManager.MODE_LIGHT -> radioLight.isChecked = true
            ThemeManager.MODE_DARK -> radioDark.isChecked = true
            ThemeManager.MODE_SYSTEM -> radioSystem.isChecked = true
        }
        updateThemeLabel(currentMode)

        // Listen for changes
        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioLight -> ThemeManager.MODE_LIGHT
                R.id.radioDark -> ThemeManager.MODE_DARK
                R.id.radioSystem -> ThemeManager.MODE_SYSTEM
                else -> ThemeManager.MODE_SYSTEM
            }

            // Save and apply theme
            ThemeManager.saveThemeMode(requireContext(), mode)
            appPreferences.themeMode = mode
            updateThemeLabel(mode)
        }
    }

    private fun updateThemeLabel(mode: Int) {
        val label = when (mode) {
            ThemeManager.MODE_LIGHT -> "Light"
            ThemeManager.MODE_DARK -> "Dark"
            ThemeManager.MODE_SYSTEM -> "System"
            else -> "System"
        }
        textViewCurrentTheme.text = label
    }

    private fun loadPreferences() {
        switchNotifications.isChecked = appPreferences.notificationsEnabled
        switchDailySummary.isChecked = appPreferences.dailySummaryEnabled
        updateTimeButton(buttonDefaultTime, appPreferences.defaultReminderTime)
        updateTimeButton(buttonSummaryTime, appPreferences.dailySummaryTime)
    }

    private fun showTimePicker(currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(time)
            },
            hour,
            minute,
            false
        ).show()
    }

    private fun updateTimeButton(button: MaterialButton, time: String) {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        button.text = timeFormat.format(calendar.time)
    }
}