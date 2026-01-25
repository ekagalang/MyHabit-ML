package com.habittracker.ml.ui.insights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.habittracker.ml.R
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.data.repository.PredictionRepository

class InsightsFragment : Fragment() {

    private lateinit var viewModel: InsightsViewModel
    private lateinit var toolbar: MaterialToolbar
    private lateinit var buttonRefresh: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var layoutContent: LinearLayout
    private lateinit var buttonGenerateNow: MaterialButton

    // Future Self Card
    private lateinit var progressScore: ProgressBar
    private lateinit var textViewScore: TextView
    private lateinit var textViewConfidence: TextView
    private lateinit var textViewDescription: TextView

    // Predictions
    private lateinit var recyclerViewPredictions: RecyclerView
    private lateinit var habitPredictionAdapter: HabitPredictionAdapter

    // Best Times
    private lateinit var textViewBestTimesTitle: TextView
    private lateinit var recyclerViewBestTimes: RecyclerView
    private lateinit var bestTimeAdapter: BestTimeAdapter

    // Last Updated
    private lateinit var textViewLastUpdated: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_insights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar)
        buttonRefresh = view.findViewById(R.id.buttonRefresh)
        progressBar = view.findViewById(R.id.progressBar)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
        layoutContent = view.findViewById(R.id.layoutContent)
        buttonGenerateNow = view.findViewById(R.id.buttonGenerateNow)

        progressScore = view.findViewById(R.id.progressScore)
        textViewScore = view.findViewById(R.id.textViewScore)
        textViewConfidence = view.findViewById(R.id.textViewConfidence)
        textViewDescription = view.findViewById(R.id.textViewDescription)

        recyclerViewPredictions = view.findViewById(R.id.recyclerViewPredictions)
        textViewBestTimesTitle = view.findViewById(R.id.textViewBestTimesTitle)
        recyclerViewBestTimes = view.findViewById(R.id.recyclerViewBestTimes)
        textViewLastUpdated = view.findViewById(R.id.textViewLastUpdated)

        // Setup ViewModel
        val database = HabitDatabase.getDatabase(requireContext())
        val habitRepository = HabitRepository(database.habitDao(), database.checkInDao())
        val predictionRepository = PredictionRepository(database.predictionDao())
        val factory = InsightsViewModelFactory(habitRepository, predictionRepository, requireContext())
        viewModel = ViewModelProvider(this, factory)[InsightsViewModel::class.java]

        // Setup RecyclerViews
        habitPredictionAdapter = HabitPredictionAdapter()
        recyclerViewPredictions.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewPredictions.adapter = habitPredictionAdapter

        bestTimeAdapter = BestTimeAdapter()
        recyclerViewBestTimes.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewBestTimes.adapter = bestTimeAdapter

        // Toolbar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Refresh button
        buttonRefresh.setOnClickListener {
            viewModel.generateNow()
        }

        // Generate now button
        buttonGenerateNow.setOnClickListener {
            viewModel.generateNow()
        }

        // Observe ViewModel
        observeViewModel()
    }

    private fun observeViewModel() {
        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                layoutContent.visibility = View.GONE
                layoutEmptyState.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
            }
        }

        // Has data
        viewModel.hasData.observe(viewLifecycleOwner) { hasData ->
            if (hasData) {
                layoutContent.visibility = View.VISIBLE
                layoutEmptyState.visibility = View.GONE
            } else {
                layoutContent.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            }
        }

        // Overall score
        viewModel.overallScore.observe(viewLifecycleOwner) { score ->
            val scoreInt = score.toInt()
            progressScore.progress = scoreInt
            textViewScore.text = scoreInt.toString()
        }

        // Confidence
        viewModel.confidence.observe(viewLifecycleOwner) { confidence ->
            val confidencePercent = (confidence * 100).toInt()
            textViewConfidence.text = "$confidencePercent%"
        }

        // Description
        viewModel.description.observe(viewLifecycleOwner) { description ->
            textViewDescription.text = description
        }

        // Habit predictions
        viewModel.habitPredictions.observe(viewLifecycleOwner) { predictions ->
            habitPredictionAdapter.submitList(predictions)
        }

        // Best times
        viewModel.bestTimes.observe(viewLifecycleOwner) { bestTimes ->
            if (bestTimes.isNotEmpty()) {
                textViewBestTimesTitle.visibility = View.VISIBLE
                recyclerViewBestTimes.visibility = View.VISIBLE
                bestTimeAdapter.submitList(bestTimes)
            } else {
                textViewBestTimesTitle.visibility = View.GONE
                recyclerViewBestTimes.visibility = View.GONE
            }
        }

        // Last updated
        viewModel.lastUpdated.observe(viewLifecycleOwner) {
            textViewLastUpdated.text = "Last updated: ${viewModel.getFormattedLastUpdated()}"
        }
    }
}