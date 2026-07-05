package com.fashnix.app.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentStyleChallengesBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StyleChallengesFragment : Fragment() {

    private var _binding: FragmentStyleChallengesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStyleChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExpertUI()
        runMasterEntrance()
        
        binding.challengeToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupExpertUI() {
        // High-end touch feedback
        binding.btnJoinMain.addExpertHoverEffect()
        
        binding.btnJoinMain.setOnClickListener {
            findNavController().navigate(R.id.plannerFragment)
        }
    }

    private fun runMasterEntrance() {
        // Luxury Reveal Animation
        binding.challengesRecycler.alpha = 0f
        binding.challengesRecycler.translationY = 120f
        binding.challengesRecycler.animate()
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
