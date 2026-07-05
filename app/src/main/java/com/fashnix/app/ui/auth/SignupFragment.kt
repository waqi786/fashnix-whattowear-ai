package com.fashnix.app.ui.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentSignupBinding
import com.fashnix.app.ui.main.MainActivity
import com.fashnix.app.util.addExpertHoverEffect
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * SignupFragment: The Identity Creation Gateway for Fashnix Luxe.
 * Engineered for secure, premium onboarding.
 */
@AndroidEntryPoint
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExpertUI()
        runEntranceAnimations()
    }

    private fun setupExpertUI() {
        // Apply high-end physical hover physics
        binding.signUpButton.addExpertHoverEffect()
        binding.loginLink.addExpertHoverEffect()
        binding.backBtn.addExpertHoverEffect()
        binding.signupCard.addExpertHoverEffect()
        binding.googleButton.addExpertHoverEffect()
        binding.facebookButton.addExpertHoverEffect()
        binding.appleButton.addExpertHoverEffect()

        // Primary Action: Validate Access (Signup)
        binding.signUpButton.setOnClickListener {
            performSignup()
        }

        // Navigation: Return to Login Hub
        binding.backBtn.setOnClickListener {
            performHapticFeedback(VibrationEffect.EFFECT_TICK)
            (requireActivity() as AuthActivity).showLogin()
        }

        binding.loginLink.setOnClickListener {
            performHapticFeedback(VibrationEffect.EFFECT_CLICK)
            (requireActivity() as AuthActivity).showLogin()
        }

        binding.googleButton.setOnClickListener {
            handleProviderLogin("google.com", "Google")
        }

        binding.facebookButton.setOnClickListener {
            handleProviderLogin("facebook.com", "Facebook")
        }

        binding.appleButton.setOnClickListener {
            handleProviderLogin("apple.com", "Apple")
        }
    }

    private fun runEntranceAnimations() {
        val animatedSequence = listOf(
            binding.signupHeader, 
            binding.signupSubHeader, 
            binding.signupCard,
            binding.loginLink
        )

        animatedSequence.forEachIndexed { index, targetView ->
            targetView.alpha = 0f
            targetView.translationY = 80f
            
            targetView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(100L + (index * 120L))
                .setInterpolator(DecelerateInterpolator(2.0f))
                .start()
        }
    }

    private fun performSignup() {
        val name = binding.nameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (!validateInputs(name, email, password)) {
            performHapticFeedback(VibrationEffect.EFFECT_DOUBLE_CLICK)
            return
        }

        performHapticFeedback(VibrationEffect.EFFECT_HEAVY_CLICK)
        toggleLoadingState(true)

        lifecycleScope.launch {
            authViewModel.signup(name, email, password, "Unisex", "Athletic").fold(
                onSuccess = {
                    toggleLoadingState(false)
                    showLuxuryToast("Welcome to Fashnix.")
                    navigateToMain()
                },
                onFailure = { e ->
                    toggleLoadingState(false)
                    showPremiumError(toFriendlyAuthMessage(e.message))
                }
            )
        }
    }

    private fun handleProviderLogin(providerId: String, label: String) {
        performHapticFeedback(VibrationEffect.EFFECT_CLICK)
        toggleLoadingState(true)
        lifecycleScope.launch {
            authViewModel.loginWithOAuthProvider(requireActivity(), providerId).fold(
                onSuccess = {
                    toggleLoadingState(false)
                    navigateToMain()
                },
                onFailure = {
                    toggleLoadingState(false)
                    showPremiumError("$label sign-in is not ready. Enable $label provider in Firebase console.")
                }
            )
        }
    }

    private fun validateInputs(name: String, email: String, pass: String): Boolean {
        var isValid = true
        
        if (name.length < 3) {
            binding.nameLayout.error = "Enter at least 3 characters"
            isValid = false
        } else binding.nameLayout.error = null

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Enter a valid email"
            isValid = false
        } else binding.emailLayout.error = null

        if (pass.length < 6) {
            binding.passwordLayout.error = "Use at least 6 characters"
            isValid = false
        } else binding.passwordLayout.error = null

        return isValid
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        binding.signUpButton.isEnabled = !isLoading
        binding.signUpButton.text = if (isLoading) "Creating account..." else "Create Account"
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun performHapticFeedback(effectId: Int) {
        val vibrator = requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(effectId))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    private fun showPremiumError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .show()
    }

    private fun toFriendlyAuthMessage(rawMessage: String?): String {
        val message = rawMessage.orEmpty().lowercase()
        return when {
            "network" in message || "offline" in message || "unreachable" in message ->
                "Please check your internet connection and try again."
            "already" in message || "collision" in message ->
                "This email already has an account. Please sign in instead."
            "password" in message || "weak" in message ->
                "Please use a stronger password with at least 6 characters."
            "email" in message ->
                "Please enter a valid email address."
            else -> "Could not create account. Please try again."
        }
    }

    private fun showLuxuryToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
