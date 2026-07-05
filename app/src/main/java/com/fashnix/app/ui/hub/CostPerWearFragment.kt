package com.fashnix.app.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentCostPerWearBinding
import com.fashnix.app.ui.wardrobe.ClothingItemAdapter
import com.fashnix.app.ui.wardrobe.WardrobeViewModel
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CostPerWearFragment : Fragment() {

    private var _binding: FragmentCostPerWearBinding? = null
    private val binding get() = _binding!!
    private val wardrobeViewModel: WardrobeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCostPerWearBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExpertUI()
        runMasterAnimations()
        observeWardrobe()

        binding.cpwToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupExpertUI() {
        // High-end touch interactions for the main analytics card
        binding.totalValueText.parent?.let { (it as View).addExpertHoverEffect() }

        val adapter = ClothingItemAdapter(
            onItemClick = { item ->
                val bundle = Bundle().apply { putString("clothingItemId", item.id) }
                findNavController().navigate(R.id.clothingItemDetailFragment, bundle)
            },
            onLongClick = {}
        )
        
        binding.cpwRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.cpwRecycler.adapter = adapter
    }

    private fun observeWardrobe() {
        wardrobeViewModel.items.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                val totalValue = items.sumOf { it.price }
                binding.totalValueText.text = "$${String.format("%.2f", totalValue)}"
                (binding.cpwRecycler.adapter as? ClothingItemAdapter)?.submitList(items)
            }
        }
        wardrobeViewModel.loadItems()
    }

    private fun runMasterAnimations() {
        // Luxury staggered reveal for the investment dashboard
        val views = listOf(binding.totalValueText.parent as View, binding.cpwRecycler)
        
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 80f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(100L + (index * 200L))
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}