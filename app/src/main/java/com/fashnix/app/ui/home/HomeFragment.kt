package com.fashnix.app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.databinding.FragmentHomeBinding
import com.fashnix.app.util.addExpertHoverEffect
import com.fashnix.app.util.SafeImageLoader
import com.fashnix.app.utils.SampleData
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import java.util.Locale

/**
 * HomeFragment: The central command dashboard of the Fashnix Luxe experience.
 * Optimized for real-time accuracy, high-end aesthetics, and intuitive navigation.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var accessoriesAdapter: OutfitPagerAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            fetchLocation()
        } else {
            showLocationRequiredWeather()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupCarousel()
        setupCommandCenter()
        checkLocationPermissions()
        observeViewModel()
        animateEntrance()
    }

    private fun setupUI() {
        listOf(binding.actionScan.root, binding.actionTryon.root, 
               binding.actionFamily.root, binding.actionLaundry.root,
               binding.capsuleShortcut.root, binding.eventShortcut.root, binding.aiShortcut.root,
               binding.pickOutfitRow.root, binding.laundryRiskRow.root, binding.missingItemRow.root,
               binding.askStylistRow.root, binding.predictionReviewRow.root, binding.rewearRow.root,
               binding.familyShareRow.root,
               binding.weatherCard, binding.morningDecisionCard, binding.careCard, binding.gapCard,
               binding.profileImage).forEach { it.addExpertHoverEffect() }
               
        binding.profileImage.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        binding.morningDecisionCard.setOnClickListener {
            performSmallHaptic()
            findNavController().navigate(R.id.action_homeFragment_to_morningSaviorFragment)
        }
        binding.careCard.setOnClickListener {
            performSmallHaptic()
            findNavController().navigate(R.id.laundryDashboardFragment)
        }
        binding.gapCard.setOnClickListener {
            performSmallHaptic()
            findNavController().navigate(R.id.gapAnalyzerFragment)
        }
        binding.pickOutfitRow.root.setOnClickListener {
            performSmallHaptic()
            findNavController().navigate(R.id.action_homeFragment_to_morningSaviorFragment)
        }
        binding.laundryRiskRow.root.setOnClickListener {
            performSmallHaptic()
            findNavController().navigate(R.id.laundryDashboardFragment)
        }
        binding.missingItemRow.root.setOnClickListener {
            performSmallHaptic()
            findNavController().navigate(R.id.gapAnalyzerFragment)
        }
        binding.askStylistRow.root.setOnClickListener {
            performSmallHaptic()
            openStylistWith("Pick a complete outfit for today and keep it practical.")
        }
        binding.predictionReviewRow.root.setOnClickListener {
            performSmallHaptic()
            findNavController().navigate(R.id.cameraFragment)
        }
        binding.rewearRow.root.setOnClickListener {
            performSmallHaptic()
            openStylistWith("Which clean item should I rewear today and how should I style it?")
        }
        binding.familyShareRow.root.setOnClickListener {
            performSmallHaptic()
            findNavController().navigate(R.id.familyClosetFragment)
        }
    }

    private fun setupCarousel() {
        accessoriesAdapter = OutfitPagerAdapter(SampleData.getSampleItems("preview").take(6)) { item ->
            performSmallHaptic()
            val bundle = Bundle().apply { putString("clothingItemId", item.id) }
            findNavController().navigate(R.id.clothingItemDetailFragment, bundle)
        }
        binding.accessoriesViewPager.apply {
            adapter = accessoriesAdapter
            offscreenPageLimit = 3
            // Elite Page Transformation for premium feel
            setPageTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.88f + r * 0.12f
                page.alpha = 0.45f + r * 0.55f
            }
        }
    }

    private fun setupCommandCenter() {
        setupSignalRows()

        // Module 1: Smart Scan
        binding.actionScan.apply {
            actionIcon.setImageResource(R.drawable.ic_camera)
            actionTitle.text = getString(R.string.action_scan)
            actionSubtitle.text = getString(R.string.neural_vision)
            actionDescription.text = ""
            root.setOnClickListener { 
                performSmallHaptic()
                findNavController().navigate(R.id.cameraFragment) 
            }
        }

        // Module 2: Virtual Mirror
        binding.actionTryon.apply {
            actionIcon.setImageResource(R.drawable.ic_sparkle)
            actionTitle.text = getString(R.string.action_mirror)
            actionSubtitle.text = getString(R.string.ar_engine)
            actionDescription.text = ""
            root.setOnClickListener { 
                performSmallHaptic()
                findNavController().navigate(R.id.tryOnFragment) 
            }
        }

        // Module 3: Family Share
        binding.actionFamily.apply {
            actionIcon.setImageResource(R.drawable.ic_family_empty)
            actionTitle.text = "Family Share"
            actionSubtitle.text = "CLOSET LINK"
            actionDescription.text = ""
            root.setOnClickListener {
                performSmallHaptic()
                findNavController().navigate(R.id.familyClosetFragment)
            }
        }

        // Module 4: Luxe Care
        binding.actionLaundry.apply {
            actionIcon.setImageResource(R.drawable.ic_laundry_basket)
            actionTitle.text = getString(R.string.action_care)
            actionSubtitle.text = getString(R.string.intelligence)
            actionDescription.text = ""
            root.setOnClickListener { 
                performSmallHaptic()
                findNavController().navigate(R.id.laundryDashboardFragment) 
            }
        }

        binding.capsuleShortcut.apply {
            actionIcon.setImageResource(R.drawable.ic_suitcase)
            actionTitle.text = "Trip Capsule"
            actionSubtitle.text = "PACK FAST"
            actionDescription.text = ""
            root.setOnClickListener {
                performSmallHaptic()
                findNavController().navigate(R.id.capsuleGeneratorFragment)
            }
        }

        binding.eventShortcut.apply {
            actionIcon.setImageResource(R.drawable.ic_calendar)
            actionTitle.text = "Event Outfit"
            actionSubtitle.text = "DRESS CODE"
            actionDescription.text = ""
            root.setOnClickListener {
                performSmallHaptic()
                findNavController().navigate(R.id.dressCodeAdvisorFragment)
            }
        }

        binding.aiShortcut.apply {
            actionIcon.setImageResource(R.drawable.ic_send)
            actionTitle.text = "Ask Stylist"
            actionSubtitle.text = "SAVE TIME"
            actionDescription.text = ""
            root.setOnClickListener {
                performSmallHaptic()
                val bundle = Bundle().apply {
                    putString("autoQuery", "I am in a hurry. Pick a complete outfit from my wardrobe for today.")
                }
                findNavController().navigate(R.id.chatFragment, bundle)
            }
        }
    }

    private fun setupSignalRows() {
        binding.pickOutfitRow.apply {
            signalIcon.setImageResource(R.drawable.ic_shuffle)
            signalTitle.text = "Pick outfit"
            signalSubtitle.text = "Visual pick"
            signalMeta.text = "10s"
        }
        binding.laundryRiskRow.apply {
            signalIcon.setImageResource(R.drawable.ic_laundry_basket)
            signalTitle.text = "Laundry risk"
            signalSubtitle.text = "Clean only"
            signalMeta.text = "CHECK"
        }
        binding.missingItemRow.apply {
            signalIcon.setImageResource(R.drawable.ic_analyze)
            signalTitle.text = "Missing item"
            signalSubtitle.text = "Fill gaps"
            signalMeta.text = "SMART"
        }
        binding.askStylistRow.apply {
            signalIcon.setImageResource(R.drawable.ic_send)
            signalTitle.text = "Ask stylist"
            signalSubtitle.text = "Chat"
            signalMeta.text = "AI"
        }
        binding.predictionReviewRow.apply {
            signalIcon.setImageResource(R.drawable.ic_camera)
            signalTitle.text = "Prediction review"
            signalSubtitle.text = "Review"
            signalMeta.text = "0"
        }
        binding.rewearRow.apply {
            signalIcon.setImageResource(R.drawable.ic_refresh)
            signalTitle.text = "Smart rewear"
            signalSubtitle.text = "Reuse"
            signalMeta.text = "READY"
        }
        binding.familyShareRow.apply {
            signalIcon.setImageResource(R.drawable.ic_family_empty)
            signalTitle.text = "Family share"
            signalSubtitle.text = "Share"
            signalMeta.text = "SHARE"
        }
    }

    private fun openStylistWith(query: String) {
        val bundle = Bundle().apply { putString("autoQuery", query) }
        findNavController().navigate(R.id.chatFragment, bundle)
    }

    private fun checkLocationPermissions() {
        val hasFine = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            fetchLocation()
        } else {
            binding.tempText.text = "--"
            binding.weatherDesc.text = "Allow location for local weather"
            binding.styleTip.text = "Enable location so Fashnix can match outfits to your city temperature."
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showLocationRequiredWeather() {
        binding.tempText.text = "--"
        binding.weatherDesc.text = "Location permission needed"
        binding.styleTip.text = "Turn on location permission to show the actual temperature for your current city."
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0L)
            .build()
        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    viewModel.loadWeather(location.latitude, location.longitude, resolveCityName(location))
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            viewModel.loadWeather(lastLocation.latitude, lastLocation.longitude, resolveCityName(lastLocation))
                        } else {
                            showLocationRequiredWeather()
                        }
                    }.addOnFailureListener {
                        showLocationRequiredWeather()
                    }
                }
            }
            .addOnFailureListener {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.loadWeather(location.latitude, location.longitude, resolveCityName(location))
                    } else {
                        showLocationRequiredWeather()
                    }
                }.addOnFailureListener {
                    showLocationRequiredWeather()
                }
            }
    }

    private fun resolveCityName(location: Location): String? {
        return runCatching {
            val addresses = Geocoder(requireContext(), Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
            val address = addresses?.firstOrNull()
            address?.locality ?: address?.subAdminArea ?: address?.adminArea
        }.getOrNull()
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.userName.text = "FASHNIX"
            SafeImageLoader.loadProfileImage(binding.profileImage, profile.photoUrl)
        }
        viewModel.weatherData.observe(viewLifecycleOwner) { data ->
            binding.tempText.text = getString(R.string.temp_format, data.temperature.toInt())
            binding.weatherDesc.text = localWeatherLabel(data.condition, data.city)
            binding.weatherIcon.setImageResource(weatherIconFor(data.condition, data.icon))
            
            binding.styleTip.text = when {
                data.temperature > 30 -> "Light cotton + breathable shoes."
                data.temperature < 15 -> "Layered outfit + clean boots."
                data.condition.contains("Rain", true) -> "Dark denim + waterproof shoes."
                else -> "Clean top + fitted bottom."
            }
        }
        viewModel.weatherError.observe(viewLifecycleOwner) { message ->
            binding.tempText.text = "--"
            binding.weatherDesc.text = message
            binding.styleTip.text = "Tell Fashnix your plan and get a complete outfit answer."
        }
        viewModel.wardrobeItems.observe(viewLifecycleOwner) {
            updateHomeIntelligence(it)
            accessoriesAdapter.updateItems(buildCuratedLooks(it))
        }
    }

    private fun weatherIconFor(condition: String, icon: String): Int {
        val value = "$condition $icon".lowercase()
        return when {
            "rain" in value || "drizzle" in value || "thunder" in value -> R.drawable.ic_rain_orange
            "cloud" in value -> R.drawable.ic_cloud_orange
            "mist" in value || "fog" in value || "haze" in value || "smoke" in value -> R.drawable.ic_fog_orange
            else -> R.drawable.ic_sun_orange
        }
    }

    private fun localWeatherLabel(condition: String, city: String): String {
        val cleanCondition = condition.ifBlank { "Weather" }.uppercase(Locale.US)
        val cleanCity = city.ifBlank { "LOCAL" }.uppercase(Locale.US).take(18)
        return "$cleanCondition - $cleanCity"
    }

    private fun buildCuratedLooks(items: List<ClothingItem>): List<ClothingItem> {
        val polishedItems = items
            .filter { item ->
                item.imageUrl.isNotBlank() &&
                    item.name.isNotBlank() &&
                    !item.imageUrl.contains("placeholder", ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<ClothingItem> { it.id.startsWith("seed_") }
                    .thenBy { it.category }
                    .thenBy { it.name }
            )

        return (polishedItems + SampleData.getSampleItems("preview"))
            .distinctBy { it.id.ifBlank { "${it.name}-${it.category}" } }
            .take(10)
    }

    private fun updateHomeIntelligence(items: List<ClothingItem>) {
        val total = items.size
        val cleanCount = items.count { it.laundryStatus.equals("Clean", true) || it.laundryStatus.isBlank() }
        val careSoon = items.count { it.wearCount >= it.laundryIntervalWears && !it.laundryStatus.equals("Dirty", true) }
        val dirtyCount = items.count { it.laundryStatus.equals("Dirty", true) }
        val repeatReady = items.count {
            (it.laundryStatus.equals("Clean", true) || it.laundryStatus.isBlank()) && it.wearCount in 1 until it.laundryIntervalWears
        }
        val categories = items.map { it.category.lowercase() }.toSet()
        val missing = listOf("Apparel", "Footwear", "Accessories")
            .filterNot { required -> categories.any { it.contains(required.lowercase()) } }
        val rewearCandidate = items
            .filter { it.laundryStatus.equals("Clean", true) || it.laundryStatus.isBlank() }
            .maxByOrNull { it.wearCount }

        binding.closetCountValue.text = total.toString()
        binding.cleanReadyValue.text = cleanCount.toString()
        binding.timeSavedValue.text = "${(12 + total.coerceAtMost(18) / 2)}m"
        binding.dirtyRiskValue.text = (dirtyCount + careSoon).toString()
        binding.repeatReadyValue.text = repeatReady.toString()
        binding.reviewQueueValue.text = "0"
        binding.laundryRiskRow.signalMeta.text = if (dirtyCount + careSoon > 0) "${dirtyCount + careSoon}" else "CLEAR"
        binding.missingItemRow.signalMeta.text = if (missing.isNotEmpty()) missing.size.toString() else "OK"
        binding.predictionReviewRow.signalMeta.text = "0"
        binding.rewearRow.signalSubtitle.text = rewearCandidate?.let {
            "${it.name.ifBlank { it.category.ifBlank { "Clean item" } }}"
        } ?: "Scan to unlock"
        binding.careStatusText.text = if (careSoon > 0) {
            "$careSoon need care"
        } else {
            "Ready"
        }
        binding.gapStatusText.text = if (missing.isNotEmpty()) {
            "Missing ${missing.joinToString(", ")}"
        } else {
            "Balanced"
        }
    }

    private fun animateEntrance() {
        listOf(
            binding.headerLayout,
            binding.weatherCard,
            binding.homeStatsStrip,
            binding.morningDecisionCard,
            binding.problemSolverRail,
            binding.accessoriesViewPager,
            binding.studioGrid
        ).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(index * 150L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun performSmallHaptic() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
