package com.fashnix.app.ui.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentCameraBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraFragment: The AI Neural Eye.
 * Fully functional with real image capture and professional feedback.
 */
@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera() else handlePermissionDenied()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        val bundle = Bundle().apply { putString("capturedImageUri", uri.toString()) }
        findNavController().navigate(R.id.scanResultFragment, bundle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        setupUI()
        checkPermissions()
        animateScanLine()
    }

    private fun setupUI() {
        binding.cameraToolbar.setNavigationOnClickListener { navigateHomeSafely() }
        
        binding.captureButton.addExpertHoverEffect()
        binding.captureButton.setOnClickListener {
            takePhoto()
            performHapticFeedback(VibrationEffect.EFFECT_HEAVY_CLICK)
        }

        binding.switchCameraButton.setOnClickListener {
            performHapticFeedback(VibrationEffect.EFFECT_CLICK)
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        binding.galleryButton.setOnClickListener {
            performHapticFeedback(VibrationEffect.EFFECT_CLICK)
            pickImageLauncher.launch("image/*")
        }
    }

    private fun startCamera() {
        val context = context ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    _binding?.viewFinder?.surfaceProvider?.let { provider -> it.setSurfaceProvider(provider) }
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("Fashnix", "Camera initialization failed", exc)
                imageCapture = null
                Toast.makeText(requireContext(), "Camera unavailable. Use gallery instead.", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Toast.makeText(requireContext(), "Camera is not ready. Try gallery or wait a moment.", Toast.LENGTH_SHORT).show()
            return
        }
        val currentBinding = _binding ?: return

        // Professional Shutter Visual Feedback
        currentBinding.root.postDelayed({
            _binding?.root?.foreground = ColorDrawable(Color.WHITE)
            _binding?.root?.postDelayed({ _binding?.root?.foreground = null }, 50)
        }, 50)

        val photoFile = File(requireContext().externalCacheDir, SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val uri = Uri.fromFile(photoFile)
                Log.d("Fashnix", "Image captured successfully: $uri")
                val bundle = Bundle().apply { putString("capturedImageUri", uri.toString()) }
                findNavController().navigate(R.id.scanResultFragment, bundle)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("Fashnix", "Capture failed: ${exception.message}")
                Toast.makeText(requireContext(), "Could not capture image. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun animateScanLine() {
        _binding?.scanLine?.let { line ->
            line.animate()
                .translationY(1500f)
                .setDuration(4000)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    _binding?.scanLine?.let { 
                        it.translationY = 0f
                        animateScanLine()
                    }
                }.start()
        }
    }

    private fun performHapticFeedback(effectId: Int) {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(effectId))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    private fun handlePermissionDenied() {
        Toast.makeText(requireContext(), "Camera access is required for AI Smart Scan.", Toast.LENGTH_LONG).show()
        navigateHomeSafely()
    }

    private fun navigateHomeSafely() {
        val navController = findNavController()
        if (!navController.popBackStack(R.id.homeFragment, false)) {
            navController.navigate(R.id.homeFragment)
        }
    }

    override fun onDestroyView() {
        _binding?.scanLine?.animate()?.cancel()
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
