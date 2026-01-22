package com.habittracker.ml.ui.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.habittracker.ml.R
import com.habittracker.ml.data.local.entities.Habit

class HabitsAdapter(
    private val onCheckInClick: (Habit) -> Unit,
    private val onHabitClick: (Habit) -> Unit,
    private val getStreakForHabit: (Long) -> Int // NEW: Function to get streak
) : ListAdapter<Habit, HabitsAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view, onCheckInClick, onHabitClick, getStreakForHabit)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HabitViewHolder(
        itemView: View,
        private val onCheckInClick: (Habit) -> Unit,
        private val onHabitClick: (Habit) -> Unit,
        private val getStreakForHabit: (Long) -> Int
    ) : RecyclerView.ViewHolder(itemView) {

        private val textViewIcon: TextView = itemView.findViewById(R.id.textViewIcon)
        private val textViewHabitName: TextView = itemView.findViewById(R.id.textViewHabitName)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        private val textViewStreak: TextView = itemView.findViewById(R.id.textViewStreak)
        private val buttonCheckIn: MaterialButton = itemView.findViewById(R.id.buttonCheckIn)

        fun bind(habit: Habit) {
            textViewIcon.text = habit.icon
            textViewHabitName.text = habit.name
            textViewDescription.text = habit.description

            // Get real streak
            val streak = getStreakForHabit(habit.id)
            textViewStreak.text = if (streak > 0) {
                "🔥 $streak day streak"
            } else {
                "Target: ${habit.targetFrequency}x per week"
            }

            buttonCheckIn.setOnClickListener {
                onCheckInClick(habit)
            }

            itemView.setOnClickListener {
                onHabitClick(habit)
            }
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}