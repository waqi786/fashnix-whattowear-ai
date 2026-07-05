package com.fashnix.app.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentRemixBinding
import com.fashnix.app.ui.home.OutfitPagerAdapter
import com.fashnix.app.ui.wardrobe.WardrobeViewModel
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RemixFragment : Fragment() {

    private var _binding: FragmentRemixBinding? = null
    private val binding get() = _binding!!
    private val wardrobeViewModel: WardrobeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRemixBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExpertUI()
        runEntranceAnimations()
        
        binding.remixToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        
        generateRemix()
    }

    private fun setupExpertUI() {
        // High-end touch interactions for the shuffle button
        binding.btnRegenerate.addExpertHoverEffect()
        binding.btnRegenerate.setOnClickListener {
            generateRemix()
        }
    }

    private fun runEntranceAnimations() {
        // Luxury staggered reveal
        binding.remixViewPager.alpha = 0f
        binding.remixViewPager.translationY = 100f
        binding.remixViewPager.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun generateRemix() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.loadingLottie.visibility = View.VISIBLE
            binding.remixViewPager.animate().alpha(0f).scaleX(0.95f).scaleY(0.95f).setDuration(300).start()
            
            // Expert AI Simulation
            delay(1500)
            
            wardrobeViewModel.items.observe(viewLifecycleOwner) { items ->
                if (items.isNotEmpty()) {
                    val shuffledOutfits = items.shuffled().take(5)
                    val adapter = OutfitPagerAdapter(shuffledOutfits) { item ->
                        val bundle = Bundle().apply { putString("clothingItemId", item.id) }
                        findNavController().navigate(R.id.clothingItemDetailFragment, bundle)
                    }
                    binding.remixViewPager.adapter = adapter
                    binding.loadingLottie.visibility = View.GONE
                    binding.remixViewPager.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).start()
                }
            }
            wardrobeViewModel.loadItems()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}