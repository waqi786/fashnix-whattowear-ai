package com.fashnix.app.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.fashnix.app.databinding.FragmentOnboardingPageBinding

class OnboardingPageFragment : Fragment() {

    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SUBTITLE = "subtitle"
        private const val ARG_ANIMATION = "animation"

        fun newInstance(title: String, subtitle: String, animationResId: Int): OnboardingPageFragment {
            val fragment = OnboardingPageFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_SUBTITLE, subtitle)
            args.putInt(ARG_ANIMATION, animationResId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            binding.title.text = it.getString(ARG_TITLE)
            binding.subtitle.text = it.getString(ARG_SUBTITLE)
            binding.lottieAnimation.setAnimation(it.getInt(ARG_ANIMATION))
        }

        runLuxuryAnimations()
    }

    private fun runLuxuryAnimations() {
        // High-end staggered reveal for onboarding elements
        val views = listOf(binding.lottieAnimation, binding.title, binding.separator, binding.subtitle)
        
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(200L + (index * 150L))
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}