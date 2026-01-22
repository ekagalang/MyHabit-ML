package com.habittracker.ml.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.habittracker.ml.R
import com.habittracker.ml.data.local.preferences.AppPreferences
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var buttonDefaultTime: MaterialButton
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
        switchNotifications = view.findViewById(R.id.switchNotifications)
        buttonDefaultTime = view.findViewById(R.id.buttonDefaultTime)
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