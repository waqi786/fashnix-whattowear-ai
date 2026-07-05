package com.fashnix.app.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.fashnix.app.databinding.ActivitySplashBinding
import com.fashnix.app.ui.auth.AuthActivity
import com.fashnix.app.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SplashActivity: The Premium Entrance to Fashnix.
 * Handles the initial branding reveal and secure routing.
 */
@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureSystemBars()
        runMasterAnimations()
    }

    private fun configureSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.BLACK
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isAppearanceLightNavigationBars = false
            }
        }
    }

    private fun runMasterAnimations() {
        binding.logo.apply {
            alpha = 1f
            scaleX = 0.92f
            scaleY = 0.92f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setInterpolator(OvershootInterpolator(1.2f))
                .withStartAction { triggerBrandingHaptic() }
                .start()
        }

        binding.appName.apply {
            alpha = 1f
            translationY = 10f
            animate()
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(180)
                .start()
        }

        binding.tagline.apply {
            alpha = 1f
            translationY = 8f
            animate()
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(260)
                .start()
        }

        lifecycleScope.launch {
            delay(2400)
            prepareForTransition()
        }
    }

    private fun triggerBrandingHaptic() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }
    }

    private fun prepareForTransition() {
        navigateToNext()
    }

    private fun navigateToNext() {
        // PRODUCTION ROUTING: Check if user is already authenticated
        val currentUser = FirebaseAuth.getInstance().currentUser
        val destination = if (currentUser != null) MainActivity::class.java else AuthActivity::class.java

        val intent = Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
