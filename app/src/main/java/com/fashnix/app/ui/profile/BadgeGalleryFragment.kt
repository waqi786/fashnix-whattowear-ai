package com.fashnix.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fashnix.app.databinding.FragmentBadgeGalleryBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BadgeGalleryFragment : Fragment() {

    private var _binding: FragmentBadgeGalleryBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBadgeGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupExpertUI()
        observeBadges()
        
        binding.badgeToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupExpertUI() {
        // Staggered Grid for a professional look
        binding.badgeRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    private fun observeBadges() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.uiState.collectLatest { state ->
                    val profile = state.userProfile
                    val badges = profile?.badges ?: emptyList()
                    val adapter = BadgeAdapter(badges)
                    binding.badgeRecycler.adapter = adapter
                    
                    if (badges.isNotEmpty()) runEntranceAnimation()
                    
                    // Update stats
                    binding.totalBadges.text = badges.size.toString()
                }
            }
        }
    }

    private fun runEntranceAnimation() {
        // Luxury staggered reveal for achievements
        binding.badgeRecycler.alpha = 0f
        binding.badgeRecycler.translationY = 80f
        binding.badgeRecycler.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(900)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}