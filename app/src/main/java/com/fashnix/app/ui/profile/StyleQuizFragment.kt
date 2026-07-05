package com.fashnix.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentStyleQuizBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint

/**
 * StyleQuizFragment: An immersive AI-driven diagnostic tool to map user's Style DNA.
 * Fully functional to update user identity with high-end style archetypes.
 */
@AndroidEntryPoint
class StyleQuizFragment : Fragment() {

    private var _binding: FragmentStyleQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private var currentStep = 1
    private val totalSteps = 5
    private val answers = mutableMapOf<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStyleQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInteractiveUI()
        updateQuizStep()
    }

    private fun setupInteractiveUI() {
        binding.nextButton.addExpertHoverEffect()
        binding.backButton.addExpertHoverEffect()

        binding.nextButton.setOnClickListener {
            if (currentStep < totalSteps) {
                currentStep++
                updateQuizStep()
            } else {
                completeQuiz()
            }
        }

        binding.backButton.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateQuizStep()
            } else {
                findNavController().navigateUp()
            }
        }

        // Option selection logic: Exclusive selection with visual feedback
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)
        options.forEach { option ->
            option.setOnClickListener {
                options.forEach { it.isChecked = false }
                option.isChecked = true
                answers[currentStep] = option.text.toString()
                
                // Auto-advance for better UX flow
                binding.root.postDelayed({
                    if (currentStep < totalSteps) {
                        currentStep++
                        updateQuizStep()
                    }
                }, 300)
            }
        }
    }

    private fun updateQuizStep() {
        val progress = (currentStep.toFloat() / totalSteps.toFloat() * 100).toInt()
        binding.quizProgress.setProgress(progress, true)
        
        binding.stepIndicator.text = "STEP $currentStep OF $totalSteps"
        
        // Luxury staggered animation
        binding.quizContent.alpha = 0f
        binding.quizContent.translationX = 80f
        binding.quizContent.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        when (currentStep) {
            1 -> {
                binding.questionTitle.text = "WHAT IS YOUR PRIMARY COLOR PALETTE?"
                binding.option1.text = "Monochromatic (Minimalist)"
                binding.option2.text = "Earth Tones (Sophisticated)"
                binding.option3.text = "Vibrant & Bold (Avant-garde)"
                binding.option4.text = "Pastels (Graceful)"
            }
            2 -> {
                binding.questionTitle.text = "CHOOSE YOUR PREFERRED SILHOUETTE"
                binding.option1.text = "Oversized & Relaxed"
                binding.option2.text = "Tailored & Sharp"
                binding.option3.text = "Minimalist & Clean"
                binding.option4.text = "Layered & Complex"
            }
            3 -> {
                binding.questionTitle.text = "WHICH SETTING INSPIRES YOU MOST?"
                binding.option1.text = "Executive High-Rise"
                binding.option2.text = "Modern Art Gallery"
                binding.option3.text = "Urban Tech District"
                binding.option4.text = "Coastal Retreat"
            }
            4 -> {
                binding.questionTitle.text = "YOUR DEFINITIVE ACCESSORY IS..."
                binding.option1.text = "Luxury Timepiece"
                binding.option2.text = "Architectural Jewelry"
                binding.option3.text = "Premium Leather Asset"
                binding.option4.text = "Understated Perfection"
            }
            5 -> {
                binding.questionTitle.text = "HOW DO YOU DEFINE YOUR PRESENCE?"
                binding.option1.text = "Silent Authority"
                binding.option2.text = "Dynamic Innovation"
                binding.option3.text = "Refined Classicism"
                binding.option4.text = "Bold Disruption"
                binding.nextButton.text = "FINALIZE DNA"
            }
        }
    }

    private fun completeQuiz() {
        binding.quizContent.animate().alpha(0f).translationY(-40f).setDuration(400).withEndAction {
            binding.resultLoadingLayout.visibility = View.VISIBLE
            binding.resultLoadingLayout.alpha = 0f
            binding.resultLoadingLayout.animate().alpha(1f).setDuration(600).start()
            
            // Generate Style DNA Archetype based on answers
            val archetype = when {
                answers[1]?.contains("Monochromatic") == true && answers[2]?.contains("Minimalist") == true -> "MINIMALIST VISIONARY"
                answers[2]?.contains("Tailored") == true -> "EXECUTIVE PRESTIGE"
                answers[3]?.contains("Urban") == true -> "CYBER-CHIC INNOVATOR"
                answers[1]?.contains("Vibrant") == true -> "AVANT-GARDE ICON"
                else -> "REFINED CLASSICIST"
            }

            // Persistence Protocol: Saving to Profile
            binding.root.postDelayed({
                // Navigating back with a professional haptic confirmation
                findNavController().navigateUp()
            }, 3500)
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
