package com.fashnix.app.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.fashnix.app.R
import com.fashnix.app.databinding.ActivityOnboardingBinding
import com.fashnix.app.ui.auth.AuthActivity
import com.fashnix.app.util.ExpertPageTransformer
import com.fashnix.app.util.addExpertHoverEffect
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

/**
 * OnboardingActivity: The Narrative Introduction to Fashnix Luxe.
 * 
 * This activity facilitates the initial user education journey through a series 
 * of high-fidelity animated pages. It is engineered to build brand desire while 
 * demonstrating the core AI value proposition.
 *
 * ENGINEERING HIGHLIGHTS:
 * - Immersive Experience: Full-screen edge-to-edge UI using modern Insets API.
 * - Physics-Based Motion: Custom 3D Parallax Page Transformer for fluid transitions.
 * - Sensory Reinforcement: Haptic feedback integrated into page scrolling and navigation.
 * - Optimized Resource Management: Uses ViewPager2 with FragmentStateAdapter for memory efficiency.
 * - Luxury Design: Adheres to the Dark Orange & Onyx visual identity.
 *
 * @author Fashnix Growth & Experience Team
 * @version 4.2.0-STABLE
 */
@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    // View Binding for performant and type-safe UI access
    private lateinit var binding: ActivityOnboardingBinding
    
    // Telemetry identifier
    private val TAG = "Fashnix_Onboarding"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.i(TAG, "[System] Initializing Onboarding Narrative...")

        // 1. Configure Full Immersive Mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 2. Initialize Binding
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Orchestrate Onboarding Infrastructure
        setupExpertOnboarding()
        
        // 4. Execute Entrance Reveal
        runEntranceSequence()
    }

    /**
     * setupExpertOnboarding: Configures the ViewPager2, Adapter, and Page Indicators.
     */
    private fun setupExpertOnboarding() {
        Log.d(TAG, "[UI] Synchronizing onboarding components...")

        val adapter = OnboardingAdapter(this)
        binding.viewPager.apply {
            this.adapter = adapter
            
            // Apply Elite 3D Parallax Transformer
            setPageTransformer(ExpertPageTransformer())
            
            // Register for Sensory Feedback
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    Log.d(TAG, "[State] Onboarding Page Selected: $position")
                    performHapticFeedback(VibrationEffect.EFFECT_TICK)
                }
            })
        }

        // Connect Premium Dot Indicator
        TabLayoutMediator(binding.dotsIndicator, binding.viewPager) { _, _ -> }.attach()

        // Configure Luxury Interaction Targets
        binding.nextButton.apply {
            addExpertHoverEffect()
            setOnClickListener {
                handleNextAction()
            }
        }

        binding.skipButton.apply {
            addExpertHoverEffect()
            setOnClickListener {
                Log.i(TAG, "[Action] User requested onboarding skip.")
                performHapticFeedback(VibrationEffect.EFFECT_CLICK)
                navigateToAuth()
            }
        }
    }

    /**
     * handleNextAction: Manages logic for moving forward in the narrative.
     */
    private fun handleNextAction() {
        val currentItem = binding.viewPager.currentItem
        val totalItems = binding.viewPager.adapter?.itemCount ?: 0

        if (currentItem < totalItems - 1) {
            binding.viewPager.currentItem = currentItem + 1
            performHapticFeedback(VibrationEffect.EFFECT_CLICK)
        } else {
            Log.i(TAG, "[Action] User completed onboarding narrative.")
            performHapticFeedback(VibrationEffect.EFFECT_HEAVY_CLICK)
            navigateToAuth()
        }
    }

    /**
     * navigateToAuth: Performs a cinematic transition to the Authentication Hub.
     */
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        
        // Elite Activity Transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }

    /**
     * runEntranceSequence: Visual reveal for the onboarding container.
     */
    private fun runEntranceSequence() {
        binding.bottomControlArea.alpha = 0f
        binding.bottomControlArea.translationY = 100f
        
        binding.bottomControlArea.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .setStartDelay(500)
            .setInterpolator(DecelerateInterpolator(2.0f))
            .start()
    }

    /**
     * performHapticFeedback: Centralized bridge for premium tactile response.
     */
    private fun performHapticFeedback(effectId: Int) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(effectId))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40)
        }
    }
}
