package com.fashnix.app.ui.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fashnix.app.R
import com.fashnix.app.data.model.StyleEvent
import com.fashnix.app.databinding.FragmentPlannerBinding
import com.fashnix.app.ui.home.HomeViewModel
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

/**
 * StylePlannerFragment: The advanced scheduling and event-planning module.
 */
@AndroidEntryPoint
class StylePlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val dateFormatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializePlannerUI()
        setupCalendarListeners()
        observePlannerData()
        runEntranceAnimations()
        
        binding.plannerToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializePlannerUI() {
        binding.addPlanButton.addExpertHoverEffect()
        
        binding.plannedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupCalendarListeners() {
        binding.styleCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = dateFormatter.format(calendar.time)
            // Logic to load plans for the selected date
        }
        
        binding.addPlanButton.setOnClickListener {
            findNavController().navigate(R.id.wardrobeFragment)
        }
    }

    private fun observePlannerData() {
        viewModel.upcomingEvents.observe(viewLifecycleOwner) { events ->
            if (!events.isNullOrEmpty()) {
                // Populate planned looks in a future update
            }
        }
    }

    private fun runEntranceAnimations() {
        val views = listOf(binding.styleCalendar, binding.plannedRecycler, binding.addPlanButton)
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(100L + (index * 150L))
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
