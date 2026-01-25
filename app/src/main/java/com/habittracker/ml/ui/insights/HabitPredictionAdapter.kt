package com.habittracker.ml.ui.insights

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.habittracker.ml.R
import com.habittracker.ml.ml.models.HabitPrediction

class HabitPredictionAdapter : ListAdapter<HabitPrediction, HabitPredictionAdapter.ViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_prediction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewHabitName: TextView = itemView.findViewById(R.id.textViewHabitName)
        private val textViewTrend: TextView = itemView.findViewById(R.id.textViewTrend)
        private val textViewPrediction: TextView = itemView.findViewById(R.id.textViewPrediction)
        private val progressCompletion: ProgressBar = itemView.findViewById(R.id.progressCompletion)
        private val textViewCompletionRate: TextView = itemView.findViewById(R.id.textViewCompletionRate)
        private val textViewProjectedStreak: TextView = itemView.findViewById(R.id.textViewProjectedStreak)

        fun bind(prediction: HabitPrediction) {
            textViewHabitName.text = prediction.habitName
            textViewPrediction.text = prediction.predictionText

            // Format trend
            val trendText = when (prediction.trend) {
                "improving" -> "📈 Improving"
                "declining" -> "📉 Declining"
                else -> "➖ Stable"
            }
            textViewTrend.text = trendText

            // Set completion rate
            val completionPercent = (prediction.completionRate * 100).toInt()
            progressCompletion.progress = completionPercent
            textViewCompletionRate.text = "$completionPercent%"

            // Set projected streak
            textViewProjectedStreak.text = "🔥 Projected 30-day streak: ${prediction.projectedStreak} days"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HabitPrediction>() {
        override fun areItemsTheSame(oldItem: HabitPrediction, newItem: HabitPrediction): Boolean {
            return oldItem.habitId == newItem.habitId
        }

        override fun areContentsTheSame(oldItem: HabitPrediction, newItem: HabitPrediction): Boolean {
            return oldItem == newItem
        }
    }
}