package com.habittracker.ml.ui.habits

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.habittracker.ml.R
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.HabitWithCheckIns
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.utils.StreakCalculator
import kotlinx.coroutines.launch
import com.habittracker.ml.utils.NotificationHelper
import com.habittracker.ml.utils.WorkManagerHelper

class HabitsListFragment : Fragment() {

    private lateinit var viewModel: HabitsViewModel
    private lateinit var adapter: HabitsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewEmpty: TextView
    private lateinit var fabAddHabit: FloatingActionButton

    private var habitsWithCheckIns: List<HabitWithCheckIns> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewHabits)
        textViewEmpty = view.findViewById(R.id.textViewEmpty)
        fabAddHabit = view.findViewById(R.id.fabAddHabit)

        val buttonSettings = view.findViewById<android.widget.ImageButton>(R.id.buttonSettings)
        val buttonInsights = view.findViewById<android.widget.ImageButton>(R.id.buttonInsights)

        // Setup ViewModel
        val database = HabitDatabase.getDatabase(requireContext())
        val repository = HabitRepository(database.habitDao(), database.checkInDao())
        val factory = HabitsViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory)[HabitsViewModel::class.java]

        // Setup RecyclerView
        adapter = HabitsAdapter(
            onCheckInClick = { habit ->
                handleCheckIn(habit)
            },
            onHabitClick = { habit ->
                // Navigate to detail
                val action = HabitsListFragmentDirections
                    .actionHabitsListToHabitDetail(habit.id)
                findNavController().navigate(action)
            },
            getStreakForHabit = { habitId ->
                getStreakForHabit(habitId)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe habits
        viewModel.allHabits.observe(viewLifecycleOwner) { habits ->
            if (habits.isEmpty()) {
                recyclerView.visibility = View.GONE
                textViewEmpty.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                textViewEmpty.visibility = View.GONE
                adapter.submitList(habits)

                // Load check-ins for streak calculation
                loadHabitsWithCheckIns()
            }
        }

        // FAB click listener
        fabAddHabit.setOnClickListener {
            findNavController().navigate(R.id.action_habitsList_to_addEditHabit)
        }

        // Settings button click listener
        buttonSettings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        // TEST NOTIFICATION BUTTON
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonTestNotification)
            ?.setOnClickListener {
                android.util.Log.d("HabitsListFragment", "🔘 Test button clicked")
                NotificationHelper.sendHabitReminder(
                    requireContext(),
                    1L,
                    "Test Habit",
                    "🎯"
                )
                Toast.makeText(requireContext(), "Notification sent!", Toast.LENGTH_SHORT).show()
            }

        // TEST PREDICTION BUTTON
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonTestPrediction)
            ?.setOnClickListener {
                android.util.Log.d("HabitsListFragment", "🤖 Generating predictions...")
                WorkManagerHelper.scheduleImmediatePrediction(requireContext())
                Toast.makeText(
                    requireContext(),
                    "Generating insights... Check logcat!",
                    Toast.LENGTH_LONG
                ).show()
            }

        // Insights button click listener
        buttonInsights.setOnClickListener {
            findNavController().navigate(R.id.action_habitsList_to_insights)
        }
    }

    private fun loadHabitsWithCheckIns() {
        lifecycleScope.launch {
            val database = HabitDatabase.getDatabase(requireContext())
            val repository = HabitRepository(database.habitDao(), database.checkInDao())
            habitsWithCheckIns = repository.getAllHabitsWithCheckIns()

            // Refresh adapter to show streaks
            adapter.notifyDataSetChanged()
        }
    }

    private fun getStreakForHabit(habitId: Long): Int {
        val habitData = habitsWithCheckIns.find { it.habit.id == habitId }
        return habitData?.let {
            StreakCalculator.calculateCurrentStreak(it.checkIns)
        } ?: 0
    }

    private fun handleCheckIn(habit: com.habittracker.ml.data.local.entities.Habit) {
        lifecycleScope.launch {
            val isCheckedIn = viewModel.isHabitCheckedInToday(habit.id)
            if (isCheckedIn) {
                Toast.makeText(
                    requireContext(),
                    "Already checked in today! ✓",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                viewModel.checkInHabit(habit.id)
                Toast.makeText(
                    requireContext(),
                    "Great job! 🎉",
                    Toast.LENGTH_SHORT
                ).show()

                // Reload to update streak
                loadHabitsWithCheckIns()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload when returning from detail screen
        loadHabitsWithCheckIns()
    }
}