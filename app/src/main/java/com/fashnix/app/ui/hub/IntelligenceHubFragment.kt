package com.fashnix.app.ui.hub

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentIntelligenceHubBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint

/**
 * IntelligenceHubFragment: The Neural Diagnostic Center.
 * Re-engineered for functional depth and visual symmetry.
 */
@AndroidEntryPoint
class IntelligenceHubFragment : Fragment() {

    private var _binding: FragmentIntelligenceHubBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIntelligenceHubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSymmetricalModules()
        setupNavigationPaths()
        animateEntrance()
    }

    private fun setupSymmetricalModules() {
        // Module 1: Style Remix
        binding.moduleRemix.apply {
            moduleTitle.text = "Style Remix"
            moduleSubtitle.text = "AI generative looks"
            moduleAnim.setAnimation(R.raw.fashion_loading)
            root.addExpertHoverEffect()
        }

        // Module 2: Gap Analyzer
        binding.moduleGap.apply {
            moduleTitle.text = "Gap Analyzer"
            moduleSubtitle.text = "Inventory audit"
            root.addExpertHoverEffect()
        }

        // Module 3: Capsule Gen
        binding.moduleCapsule.apply {
            moduleTitle.text = "Capsule Gen"
            moduleSubtitle.text = "Travel intelligence"
            root.addExpertHoverEffect()
        }

        // Module 4: Analytics
        binding.moduleAnalytics.apply {
            moduleTitle.text = "Analytics"
            moduleSubtitle.text = "Investment tracking"
            root.addExpertHoverEffect()
        }

        // Module 5: Style DNA
        binding.moduleDna.apply {
            moduleTitle.text = "Style DNA"
            moduleSubtitle.text = "Identity mapping"
            root.addExpertHoverEffect()
        }

        // Module 6: Sustainability
        binding.moduleSustainability.apply {
            moduleTitle.text = "Eco Impact"
            moduleSubtitle.text = "Impact analysis"
            root.addExpertHoverEffect()
        }
    }

    private fun setupNavigationPaths() {
        binding.moduleRemix.root.setOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_CLICK)
            findNavController().navigate(R.id.remixFragment) 
        }
        
        binding.moduleGap.root.setOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_CLICK)
            findNavController().navigate(R.id.gapAnalyzerFragment) 
        }
        
        binding.moduleCapsule.root.setOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_CLICK)
            findNavController().navigate(R.id.capsuleGeneratorFragment) 
        }
        
        binding.moduleAnalytics.root.setOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_CLICK)
            findNavController().navigate(R.id.wardrobeAnalyticsFragment) 
        }
        
        binding.moduleDna.root.setOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_CLICK)
            findNavController().navigate(R.id.styleQuizFragment) 
        }
        
        binding.moduleSustainability.root.setOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_CLICK)
            findNavController().navigate(R.id.wardrobeAnalyticsFragment)
        }

        binding.eventAdvisorCard.setOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_CLICK)
            findNavController().navigate(R.id.dressCodeAdvisorFragment) 
        }
        
        binding.styleChallengesCard.setOnClickListener { 
            performHaptic(VibrationEffect.EFFECT_HEAVY_CLICK)
            findNavController().navigate(R.id.styleChallengesFragment) 
        }
        
        binding.hubToolbar.setNavigationOnClickListener {
            performHaptic(VibrationEffect.EFFECT_TICK)
            findNavController().navigateUp()
        }
    }

    private fun animateEntrance() {
        val modules = listOf(
            binding.moduleRemix.root, binding.moduleGap.root,
            binding.moduleCapsule.root, binding.moduleAnalytics.root,
            binding.moduleDna.root, binding.moduleSustainability.root,
            binding.eventAdvisorCard, binding.styleChallengesCard
        )

        modules.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 60f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(100L + (index * 80L))
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun performHaptic(effectId: Int) {
        val v = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            v.vibrate(VibrationEffect.createPredefined(effectId))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
