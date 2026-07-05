package com.fashnix.app.ui.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fashnix.app.R
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.databinding.FragmentScanResultBinding
import com.fashnix.app.domain.ClassificationResult
import com.fashnix.app.domain.FashnixClassifier
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ScanResultFragment: The AI Visualization Hub.
 * Optimized for professional feedback and real-time AI results.
 */
@AndroidEntryPoint
class ScanResultFragment : Fragment() {

    private var _binding: FragmentScanResultBinding? = null
    private val binding get() = _binding!!
    
    private val scanViewModel: ScanViewModel by viewModels()
    private val args: ScanResultFragmentArgs by navArgs()
    
    @Inject lateinit var authRepository: AuthRepository
    private lateinit var classifier: FashnixClassifier
    private var needsRetake = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        classifier = FashnixClassifier(requireContext())
        setupUI()
        observeScanState()
        loadAndProcessImage()
        setupActions()
    }

    private fun setupUI() {
        binding.resultToolbar.setNavigationOnClickListener { navigateBackSafely() }
        binding.addToWardrobeButton.addExpertHoverEffect()
        binding.tryOnButton.addExpertHoverEffect()
        binding.accessoriesButton.addExpertHoverEffect()
        
        // Setup proper Labels for rows
        binding.rowCategory.rowLabel.text = "CATEGORY"
        binding.rowColour.rowLabel.text = "PRIMARY COLOUR"
        binding.rowOccasion.rowLabel.text = "STYLE CONTEXT"
        binding.rowGender.rowLabel.text = "TARGET SEGMENT"

        binding.analysisStatus.text = "STARTING STYLE ANALYSIS..."
        binding.addToWardrobeButton.isEnabled = false
        binding.tryOnButton.isEnabled = false
    }

    private fun observeScanState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    scanViewModel.isClassifying.collectLatest { isLoading ->
                        val result = scanViewModel.classificationResult.value
                        binding.analysisStatus.text = when {
                            isLoading -> "ANALYZING OUTFIT..."
                            result?.categoryConfidence != null && result.categoryConfidence < 0.35f ->
                                "LOW CONFIDENCE - PLEASE REVIEW BEFORE SAVING"
                            else -> "ANALYSIS COMPLETE"
                        }
                        val statusColor = when {
                            isLoading -> R.color.primary
                            result?.categoryConfidence != null && result.categoryConfidence < 0.35f -> R.color.warning
                            else -> R.color.success
                        }
                        binding.analysisStatus.setTextColor(resources.getColor(statusColor, null))
                    }
                }
                launch {
                    scanViewModel.classificationResult.collectLatest { result ->
                        result?.let { displayResults(it) }
                    }
                }
            }
        }
    }

    private fun loadAndProcessImage() {
        val imageUri = args.capturedImageUri
        if (!imageUri.isNullOrEmpty()) {
            val uri = Uri.parse(imageUri)
            Glide.with(this).load(uri).centerCrop().into(binding.scannedImage)
            
            lifecycleScope.launch {
                val bitmap = loadBitmap(uri)
                bitmap?.let {
                    scanViewModel.classifyImage(classifier, it)
                }
            }
        } else {
            showRetakeState("NO PHOTO FOUND - TAKE OR SELECT A GARMENT PHOTO")
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            val decoded = if (uri.scheme.equals("file", ignoreCase = true)) {
                BitmapFactory.decodeFile(uri.path)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            if (decoded?.config == Bitmap.Config.ARGB_8888) {
                decoded
            } else {
                decoded?.copy(Bitmap.Config.ARGB_8888, false)
            }
        } catch (e: Exception) {
            showRetakeState("IMAGE LOAD FAILED - TRY ANOTHER PHOTO")
            null
        }
    }

    private fun displayResults(result: ClassificationResult) {
        binding.rowCategory.rowValue.text = formatPrediction(result.category, result.categoryConfidence, 0.55f)
        binding.rowColour.rowValue.text = formatPrediction(result.colour, result.colourConfidence, 0.45f)
        binding.rowOccasion.rowValue.text = formatPrediction(result.occasion, result.occasionConfidence, 0.45f)
        binding.rowGender.rowValue.text = formatPrediction(result.gender, result.genderConfidence, 0.45f)
        needsRetake = result.categoryConfidence < 0.55f
        binding.analysisStatus.text = if (needsRetake) {
            "LOW CONFIDENCE - PLEASE REVIEW BEFORE SAVING"
        } else {
            "ANALYSIS COMPLETE"
        }
        
        binding.addToWardrobeButton.isEnabled = true
        binding.tryOnButton.isEnabled = true
        binding.addToWardrobeButton.text = if (needsRetake) "RETAKE CLEAR GARMENT PHOTO" else "ADD TO DIGITAL CLOSET"
    }

    private fun navigateBackSafely() {
        val navController = findNavController()
        if (!navController.popBackStack()) {
            navController.navigate(R.id.homeFragment)
        }
    }

    private fun formatPrediction(label: String, confidence: Float, threshold: Float): String {
        val percent = (confidence.coerceIn(0f, 1f) * 100).toInt()
        val cleanLabel = label.ifBlank { "Needs review" }.uppercase()
        return if (confidence < threshold) {
            "$cleanLabel - REVIEW ($percent%)"
        } else {
            "$cleanLabel ($percent%)"
        }
    }

    private fun setupActions() {
        binding.addToWardrobeButton.setOnClickListener {
            if (needsRetake) {
                findNavController().navigate(R.id.cameraFragment)
            } else {
                saveItemToVault()
            }
        }
        binding.tryOnButton.setOnClickListener { findNavController().navigate(R.id.tryOnFragment) }
        binding.accessoriesButton.setOnClickListener {
            val result = scanViewModel.classificationResult.value
            val query = if (result != null) {
                "Match accessories for a ${result.colour} ${result.category} for ${result.occasion}. Give me 3 practical options."
            } else {
                "Match accessories for this scanned outfit. Give me 3 practical options."
            }
            val bundle = Bundle().apply { putString("autoQuery", query) }
            findNavController().navigate(R.id.chatFragment, bundle)
        }
    }

    private fun showRetakeState(message: String) {
        needsRetake = true
        binding.analysisStatus.text = message
        binding.analysisStatus.setTextColor(resources.getColor(R.color.error, null))
        binding.rowCategory.rowValue.text = "PHOTO REQUIRED"
        binding.rowColour.rowValue.text = "PHOTO REQUIRED"
        binding.rowOccasion.rowValue.text = "PHOTO REQUIRED"
        binding.rowGender.rowValue.text = "PHOTO REQUIRED"
        binding.addToWardrobeButton.isEnabled = true
        binding.addToWardrobeButton.text = "TAKE CLEAR GARMENT PHOTO"
        binding.tryOnButton.isEnabled = false
    }

    private fun saveItemToVault() {
        val result = scanViewModel.classificationResult.value ?: return
        if (authRepository.getCurrentUserId() == null) {
            Toast.makeText(requireContext(), "Please sign in before saving items.", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val duplicate = scanViewModel.findDuplicateCandidate(result)
            if (duplicate != null) {
                showDuplicateWarning(duplicate.name.ifBlank { "${duplicate.color} ${duplicate.category}" })
                return@launch
            }

            binding.addToWardrobeButton.isEnabled = false
            binding.addToWardrobeButton.text = "SAVING..."
            
            val saveResult = scanViewModel.saveScanAndItem(
                result = result,
                imageUri = args.capturedImageUri.orEmpty(),
                userSelectedColour = null,
                occasionOverride = null
            )
            if (saveResult.isSuccess) {
                Toast.makeText(requireContext(), "Saved to wardrobe", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.wardrobeFragment)
            } else {
                Toast.makeText(requireContext(), "Wardrobe save failed", Toast.LENGTH_SHORT).show()
                binding.addToWardrobeButton.isEnabled = true
                binding.addToWardrobeButton.text = "Retry"
            }
        }
    }

    private fun showDuplicateWarning(existingName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Possible duplicate")
            .setMessage("This looks similar to $existingName already in your wardrobe.")
            .setPositiveButton("Open wardrobe") { _, _ ->
                findNavController().navigate(R.id.wardrobeFragment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        classifier.close()
        _binding = null
    }
}
