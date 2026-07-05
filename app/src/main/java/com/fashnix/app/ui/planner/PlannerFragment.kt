package com.fashnix.app.ui.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentPlannerBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlannerViewModel by viewModels()
    private lateinit var adapter: OutfitAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExpertUI()
        observeViewModel()
        runEntranceAnimations()
        
        binding.plannerToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        
        // Initial load for today
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadOutfitsForDate(System.currentTimeMillis())
        }
    }

    private fun setupExpertUI() {
        binding.addPlanButton.addExpertHoverEffect()

        adapter = OutfitAdapter(
            onMarkWorn = { outfit ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.markOutfitWorn(outfit)
                }
            },
            onDelete = { outfit ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteOutfit(outfit.id)
                }
            }
        )

        binding.plannedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlannerFragment.adapter
        }

        binding.addPlanButton.setOnClickListener {
            val selectedDate = System.currentTimeMillis() // Defaulting to today or getting from calendar
            val bundle = Bundle().apply { putLong("selectedDate", selectedDate) }
            findNavController().navigate(R.id.wardrobeFragment, bundle)
        }

        binding.styleCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.loadOutfitsForDate(calendar.timeInMillis)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.outfits.collectLatest { outfits ->
                adapter.submitList(outfits)
            }
        }
    }

    private fun runEntranceAnimations() {
        val views = listOf(binding.styleCalendar, binding.plannedRecycler)
        
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 80f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(index * 150L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}