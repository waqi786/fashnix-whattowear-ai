package com.fashnix.app.ui.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fashnix.app.databinding.FragmentCreateOutfitBinding
import com.fashnix.app.ui.wardrobe.ClothingItemAdapter
import com.fashnix.app.util.addExpertHoverEffect
import com.fashnix.app.util.animateEntrance
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateOutfitFragment : Fragment() {

    private var _binding: FragmentCreateOutfitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlannerViewModel by viewModels()
    private lateinit var itemAdapter: ClothingItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateOutfitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCanvasUI()
        setupInventoryGrid()
        observeData()
        
        binding.createToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.saveOutfitButton.setOnClickListener {
            // Luxury haptic feedback and finalize look
            findNavController().navigateUp()
        }
        
        binding.root.animateEntrance(0)
    }

    private fun setupCanvasUI() {
        // High-end touch feedback for workspace tools
        binding.canvasCard.addExpertHoverEffect()
        binding.saveOutfitButton.addExpertHoverEffect()
    }

    private fun setupInventoryGrid() {
        itemAdapter = ClothingItemAdapter(
            onItemClick = { item ->
                // Add item to professional canvas mockup
            }
        )

        binding.itemGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = itemAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeData() {
        viewModel.wardrobeItems.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items)
        }
        viewModel.loadWardrobe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}