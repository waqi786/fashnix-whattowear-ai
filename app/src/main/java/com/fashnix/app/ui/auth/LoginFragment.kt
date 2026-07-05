package com.fashnix.app.ui.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentLoginBinding
import com.fashnix.app.ui.main.MainActivity
import com.fashnix.app.util.addExpertHoverEffect
import com.fashnix.app.util.animateEntrance
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * LoginFragment: The Security Gateway for the Fashnix Luxe Ecosystem.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runEntranceSequence()
        setupLuxuryInteractions()
        initializeValidationEngine()
    }

    private fun runEntranceSequence() {
        binding.authLogo.animateEntrance(0)
        binding.welcomeTitle.animateEntrance(1)
        binding.welcomeSubtitle.animateEntrance(2)
        binding.loginCard.animateEntrance(3)
        binding.signUpLink.animateEntrance(4)
    }

    private fun setupLuxuryInteractions() {
        binding.loginButton.addExpertHoverEffect()
        binding.loginCard.addExpertHoverEffect()
        binding.googleButton.addExpertHoverEffect()
        binding.facebookButton.addExpertHoverEffect()
        binding.appleButton.addExpertHoverEffect()

        binding.signUpLink.setOnClickListener {
            performHapticFeedback(VibrationEffect.EFFECT_CLICK)
            (requireActivity() as AuthActivity).showSignup()
        }

        binding.loginButton.setOnClickListener {
            handleLoginAttempt()
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

        binding.forgotPassword.setOnClickListener {
            performHapticFeedback(VibrationEffect.EFFECT_TICK)
            val email = binding.emailEditText.text.toString().trim()
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                lifecycleScope.launch {
                    viewModel.resetPassword(email).fold(
                        onSuccess = { showPremiumToast("Reset link sent to your email.") },
                        onFailure = { showPremiumError("Failed to send reset email.") }
                    )
                }
            } else {
                showPremiumToast("Please enter a valid email first.")
            }
        }
    }

    private fun initializeValidationEngine() {
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmailFormat(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswordStrength(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun handleLoginAttempt() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (validateEmailFormat(email) && validatePasswordStrength(password)) {
            performHapticFeedback(VibrationEffect.EFFECT_HEAVY_CLICK)
            toggleLoadingState(true)
            
            lifecycleScope.launch {
                viewModel.login(email, password).fold(
                    onSuccess = {
                        toggleLoadingState(false)
                        navigateToMain()
                    },
                    onFailure = { e ->
                        toggleLoadingState(false)
                        showPremiumError(toFriendlyAuthMessage(e.message))
                    }
                )
            }
        } else {
            performHapticFeedback(VibrationEffect.EFFECT_DOUBLE_CLICK)
        }
    }

    private fun handleProviderLogin(providerId: String, label: String) {
        performHapticFeedback(VibrationEffect.EFFECT_CLICK)
        toggleLoadingState(true)
        lifecycleScope.launch {
            viewModel.loginWithOAuthProvider(requireActivity(), providerId).fold(
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

    private fun validateEmailFormat(email: String): Boolean {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = null
            true
        } else {
            if (email.isNotEmpty()) binding.emailLayout.error = "Invalid Email Format"
            false
        }
    }

    private fun validatePasswordStrength(password: String): Boolean {
        return if (password.length >= 6) {
            binding.passwordLayout.error = null
            true
        } else {
            if (password.isNotEmpty()) binding.passwordLayout.error = "Use at least 6 characters"
            false
        }
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.loginButton.text = if (isLoading) "Signing in..." else "Login"
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

    private fun showPremiumToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
            "password" in message || "credential" in message || "auth credential" in message ->
                "Email or password is incorrect."
            "user" in message && ("not found" in message || "record" in message) ->
                "No account found with this email."
            "blocked" in message || "too many" in message ->
                "Too many attempts. Please wait a moment and try again."
            else -> "Could not sign in. Please check your details and try again."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
