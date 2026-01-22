package com.habittracker.ml.ui.habits

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.habittracker.ml.R
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.HabitWithCheckIns
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.utils.StreakCalculator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*

class HabitDetailFragment : Fragment() {

    private val args: HabitDetailFragmentArgs by navArgs()
    private lateinit var viewModel: HabitsViewModel
    private lateinit var toolbar: MaterialToolbar
    private lateinit var textViewIcon: TextView
    private lateinit var textViewHabitName: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var textViewCurrentStreak: TextView
    private lateinit var textViewLongestStreak: TextView
    private lateinit var textViewTotalCheckIns: TextView
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var buttonEdit: MaterialButton
    private lateinit var buttonDelete: MaterialButton

    private var habitWithCheckIns: HabitWithCheckIns? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habit_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar)
        textViewIcon = view.findViewById(R.id.textViewIcon)
        textViewHabitName = view.findViewById(R.id.textViewHabitName)
        textViewDescription = view.findViewById(R.id.textViewDescription)
        textViewCurrentStreak = view.findViewById(R.id.textViewCurrentStreak)
        textViewLongestStreak = view.findViewById(R.id.textViewLongestStreak)
        textViewTotalCheckIns = view.findViewById(R.id.textViewTotalCheckIns)
        calendarView = view.findViewById(R.id.calendarView)
        buttonEdit = view.findViewById(R.id.buttonEdit)
        buttonDelete = view.findViewById(R.id.buttonDelete)

        // Setup ViewModel
        val database = HabitDatabase.getDatabase(requireContext())
        val repository = HabitRepository(database.habitDao(), database.checkInDao())
        val factory = HabitsViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory)[HabitsViewModel::class.java]

        // Toolbar navigation
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Load habit details
        loadHabitDetails()

        // Button listeners
        buttonEdit.setOnClickListener {
            // TODO: Navigate to edit screen
            Toast.makeText(requireContext(), "Edit feature coming soon", Toast.LENGTH_SHORT).show()
        }

        buttonDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadHabitDetails() {
        lifecycleScope.launch {
            val database = HabitDatabase.getDatabase(requireContext())
            val repository = HabitRepository(database.habitDao(), database.checkInDao())

            habitWithCheckIns = repository.getHabitWithCheckIns(args.habitId)

            habitWithCheckIns?.let { data ->
                displayHabitInfo(data)
                displayStats(data)
                displayCalendar(data)
            }
        }
    }

    private fun displayHabitInfo(data: HabitWithCheckIns) {
        textViewIcon.text = data.habit.icon
        textViewHabitName.text = data.habit.name
        textViewDescription.text = data.habit.description
    }

    private fun displayStats(data: HabitWithCheckIns) {
        val currentStreak = StreakCalculator.calculateCurrentStreak(data.checkIns)
        val longestStreak = StreakCalculator.calculateLongestStreak(data.checkIns)
        val totalCheckIns = data.checkIns.size

        textViewCurrentStreak.text = currentStreak.toString()
        textViewLongestStreak.text = longestStreak.toString()
        textViewTotalCheckIns.text = totalCheckIns.toString()
    }

    private fun displayCalendar(data: HabitWithCheckIns) {
        // Get check-in dates
        val checkInDates = data.checkIns.map { checkIn ->
            parseDate(checkIn.date)
        }.filterNotNull()

        // Add decorator for check-in dates
        calendarView.addDecorator(CheckInDecorator(checkInDates))
    }

    private fun parseDate(dateString: String): CalendarDay? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(dateString)
            date?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it
                val localDate = LocalDate.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                CalendarDay.from(localDate)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteHabit()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteHabit() {
        viewModel.deleteHabit(args.habitId)
        Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    // Calendar decorator for check-in dates
    private class CheckInDecorator(private val dates: List<CalendarDay>) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(8f, Color.parseColor("#10B981")))
        }
    }
}