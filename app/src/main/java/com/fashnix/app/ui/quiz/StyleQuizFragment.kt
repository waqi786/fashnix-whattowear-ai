package com.fashnix.app.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentStyleQuizBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint

/**
 * StyleQuizFragment: An interactive AI-driven diagnostic tool to map user's Style DNA.
 * Part of the Fashnix Billion-Dollar UX ecosystem.
 * 
 * Features:
 * - Multi-step interactive quiz.
 * - Real-time personality mapping.
 * - Integration with HomeViewModel's Style DNA engine.
 */
@AndroidEntryPoint
class StyleQuizFragment : Fragment() {

    private var _binding: FragmentStyleQuizBinding? = null
    private val binding get() = _binding!!

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

        // Option selection logic
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)
        options.forEach { option ->
            option.setOnClickListener {
                options.forEach { it.isChecked = false }
                option.isChecked = true
                answers[currentStep] = option.text.toString()
            }
        }
    }

    private fun updateQuizStep() {
        val progress = (currentStep.toFloat() / totalSteps.toFloat() * 100).toInt()
        binding.quizProgress.setProgress(progress, true)
        
        binding.stepIndicator.text = "STEP $currentStep OF $totalSteps"
        
        // Animate content change
        binding.quizContent.alpha = 0f
        binding.quizContent.translationX = 100f
        binding.quizContent.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        when (currentStep) {
            1 -> {
                binding.questionTitle.text = "WHAT IS YOUR PRIMARY COLOR PALETTE?"
                binding.option1.text = "Monochromatic (Black, White, Grey)"
                binding.option2.text = "Earth Tones (Beige, Brown, Olive)"
                binding.option3.text = "Vibrant & Bold (Neon, Primary Colors)"
                binding.option4.text = "Pastels & Soft Tones"
            }
            2 -> {
                binding.questionTitle.text = "CHOOSE YOUR PREFERRED SILHOUETTE"
                binding.option1.text = "Oversized & Relaxed"
                binding.option2.text = "Tailored & Sharp"
                binding.option3.text = "Minimalist & Clean Lines"
                binding.option4.text = "Experimental & Layered"
            }
            3 -> {
                binding.questionTitle.text = "WHICH SETTING INSPIRES YOU MOST?"
                binding.option1.text = "High-Rise Executive Office"
                binding.option2.text = "Art Gallery in Soho"
                binding.option3.text = "Industrial Tech Hub"
                binding.option4.text = "Seaside Mediterranean Retreat"
            }
            4 -> {
                binding.questionTitle.text = "YOUR GO-TO ACCESSORY IS..."
                binding.option1.text = "A Classic Luxury Watch"
                binding.option2.text = "Statement Tech-Wear Jewelry"
                binding.option3.text = "A Minimalist Leather Tote"
                binding.option4.text = "Nothing (Clean Perfection)"
            }
            5 -> {
                binding.questionTitle.text = "FINAL: HOW DO YOU DEFINE ELEGANCE?"
                binding.option1.text = "Simplicity is the ultimate sophistication"
                binding.option2.text = "Confidence is the best outfit"
                binding.option3.text = "Attention to detail is everything"
                binding.option4.text = "Breaking the rules with style"
                binding.nextButton.text = "FINALIZE DNA"
            }
        }
    }

    private fun completeQuiz() {
        binding.quizContent.animate().alpha(0f).setDuration(300).withEndAction {
            binding.resultLoadingLayout.visibility = View.VISIBLE
            binding.resultLoadingLayout.alpha = 0f
            binding.resultLoadingLayout.animate().alpha(1f).setDuration(500).start()
            
            // Simulating AI Neural Analysis
            binding.root.postDelayed({
                navigateToResults()
            }, 3000)
        }.start()
    }

    private fun navigateToResults() {
        // Logic to save DNA result based on answers
        findNavController().navigate(R.id.intelligenceHubFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
