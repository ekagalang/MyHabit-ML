package com.habittracker.ml.ui.insights

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.habittracker.ml.R
import com.habittracker.ml.ml.models.BestTimeRecommendation
import java.text.SimpleDateFormat
import java.util.*

class BestTimeAdapter : ListAdapter<BestTimeRecommendation, BestTimeAdapter.ViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_best_time, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewHabitName: TextView = itemView.findViewById(R.id.textViewHabitName)
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val textViewSuccessRate: TextView = itemView.findViewById(R.id.textViewSuccessRate)

        fun bind(recommendation: BestTimeRecommendation) {
            textViewHabitName.text = recommendation.habitName

            // Format time
            val formattedTime = formatTime(recommendation.recommendedTime)
            textViewTime.text = "Best time: $formattedTime"

            // Format success rate
            val successPercent = (recommendation.successRate * 100).toInt()
            textViewSuccessRate.text = "$successPercent%"
        }

        private fun formatTime(time: String): String {
            return try {
                val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(time)
                date?.let { outputFormat.format(it) } ?: time
            } catch (e: Exception) {
                time
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BestTimeRecommendation>() {
        override fun areItemsTheSame(
            oldItem: BestTimeRecommendation,
            newItem: BestTimeRecommendation
        ): Boolean {
            return oldItem.habitId == newItem.habitId
        }

        override fun areContentsTheSame(
            oldItem: BestTimeRecommendation,
            newItem: BestTimeRecommendation
        ): Boolean {
            return oldItem == newItem
        }
    }
}