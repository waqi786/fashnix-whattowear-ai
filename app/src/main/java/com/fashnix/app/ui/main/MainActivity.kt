package com.fashnix.app.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.fashnix.app.LocaleHelper
import com.fashnix.app.R
import com.fashnix.app.databinding.ActivityMainBinding
import com.fashnix.app.ui.auth.AuthActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity: The Orchestrator of the Fashnix Luxe Experience.
 * Re-engineered for an edge-to-edge elite UI with floating glass navigation.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var dockNavItems: List<DockNavItem>
    private val topLevelDestinations = setOf(
        R.id.homeFragment,
        R.id.wardrobeFragment,
        R.id.intelligenceHubFragment,
        R.id.profileFragment
    )
    private val selfToolbarDestinations = setOf(
        R.id.laundryDashboardFragment,
        R.id.gapAnalyzerFragment,
        R.id.familyClosetFragment,
        R.id.plannerFragment,
        R.id.styleChallengesFragment,
        R.id.dressCodeAdvisorFragment,
        R.id.capsuleGeneratorFragment
    )

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("fashnix_prefs_fast", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("selected_language", "en") ?: "en"
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(android.content.Intent(this, AuthActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }
        
        // Keep app toolbars below the status bar so back arrows sit in the normal Android position.
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarColors()

        setupNavigationCore()
        applyProfessionalInsets()
        configureDestinationListeners()
    }

    private fun applyProfessionalInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            binding.appBarLayout.updatePadding(top = 0)
            
            // Adjust bottom floating nav margin based on system navigation bars (gesture bar)
        binding.bottomNavCard.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            val density = resources.displayMetrics.density
            val horizontalMargin = (22 * density).toInt()
            val bottomMargin = (12 * density).toInt()
            setMargins(horizontalMargin, 0, horizontalMargin, systemBars.bottom + bottomMargin)
        }
            insets
        }
    }

    private fun applySystemBarColors() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)
        window.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.background)))
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    private fun setupNavigationCore() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Find toolbar via root to avoid viewbinding issues during fast refactors
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.homeFragment,
                R.id.wardrobeFragment,
                R.id.intelligenceHubFragment,
                R.id.chatFragment,
                R.id.plannerFragment,
                R.id.profileFragment
            )
        )
        
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupDockNavigation()
        binding.centerScanFab.setOnClickListener {
            if (navController.currentDestination?.id != R.id.cameraFragment) {
                performNavigationHaptic()
                navController.navigate(R.id.cameraFragment)
            }
        }
    }

    private fun setupDockNavigation() {
        dockNavItems = listOf(
            DockNavItem(binding.navHomeItem, binding.navHomeIcon, binding.navHomeLabel, R.id.homeFragment),
            DockNavItem(binding.navClosetItem, binding.navClosetIcon, binding.navClosetLabel, R.id.wardrobeFragment),
            DockNavItem(binding.navAiItem, binding.navAiIcon, binding.navAiLabel, R.id.intelligenceHubFragment),
            DockNavItem(binding.navProfileItem, binding.navProfileIcon, binding.navProfileLabel, R.id.profileFragment)
        )
        dockNavItems.forEach { navItem ->
            navItem.container.setOnClickListener {
                if (navItem.destinationId != navController.currentDestination?.id) {
                    navigateToTopLevelDestination(navItem.destinationId)
                }
            }
        }
        updateDockNavSelection(navController.currentDestination?.id)
    }

    private fun navigateToTopLevelDestination(destinationId: Int) {
        performNavigationHaptic()
        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(navController.graph.startDestinationId, false, true)
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .build()
        navController.navigate(destinationId, null, navOptions)
    }

    private fun updateDockNavSelection(destinationId: Int?) {
        if (!::dockNavItems.isInitialized) return
        val activeColor = ContextCompat.getColor(this, R.color.primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.nav_item_unselected)
        dockNavItems.forEach { item ->
            val isSelected = item.destinationId == destinationId
            val color = if (isSelected) activeColor else inactiveColor
            item.icon.setColorFilter(color)
            item.label.setTextColor(color)
            item.container.background = if (isSelected) {
                ContextCompat.getDrawable(this, R.drawable.bg_nav_item_active)
            } else {
                null
            }
            item.container.alpha = if (isSelected) 1f else 0.82f
        }
    }

    private data class DockNavItem(
        val container: View,
        val icon: ImageView,
        val label: TextView,
        val destinationId: Int
    )

    private fun configureDestinationListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateDockNavSelection(destination.id)
            when (destination.id) {
                // Hide system UI components for immersive photography/style sessions
                R.id.cameraFragment, R.id.tryOnFragment, R.id.scanResultFragment,
                R.id.chatFragment, R.id.styleQuizFragment, R.id.clothingItemDetailFragment -> {
                    toggleSystemUI(
                        isNavVisible = false,
                        isToolbarVisible = destination.id !in setOf(
                            R.id.cameraFragment,
                            R.id.scanResultFragment,
                            R.id.chatFragment,
                            R.id.tryOnFragment,
                            R.id.styleQuizFragment,
                            R.id.clothingItemDetailFragment
                        )
                    )
                }
                else -> {
                    val isTopLevel = destination.id in topLevelDestinations
                    toggleSystemUI(
                        isNavVisible = isTopLevel,
                        isToolbarVisible = !isTopLevel && destination.id !in selfToolbarDestinations
                    )
                }
            }
        }
    }

    private fun toggleSystemUI(isNavVisible: Boolean, isToolbarVisible: Boolean) {
        binding.appBarLayout.visibility = if (isToolbarVisible) View.VISIBLE else View.GONE
        binding.navHostFragment.post {
            binding.navHostFragment.updatePadding(
                top = if (isToolbarVisible) binding.appBarLayout.height else 0
            )
        }
        if (isNavVisible) {
            binding.bottomNavScrim.apply {
                animate().cancel()
                visibility = View.VISIBLE
                alpha = 0f
                animate().alpha(1f).setDuration(220).start()
            }
            binding.bottomNavCard.apply {
                animate().cancel()
                visibility = View.VISIBLE
                alpha = 0f
                translationY = 56f
                animate().alpha(1f).translationY(0f).setDuration(220).start()
            }
            binding.centerScanFab.apply {
                animate().cancel()
                visibility = View.VISIBLE
                alpha = 0f
                scaleX = 0.9f
                scaleY = 0.9f
                animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220).start()
            }
        } else {
            binding.bottomNavScrim.animate().cancel()
            binding.bottomNavScrim.visibility = View.GONE
            binding.bottomNavCard.animate().cancel()
            binding.bottomNavCard.visibility = View.GONE
            binding.centerScanFab.animate().cancel()
            binding.centerScanFab.visibility = View.GONE
        }
    }

    private fun performNavigationHaptic() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_POST_NOTIFICATIONS
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1201
    }
}
