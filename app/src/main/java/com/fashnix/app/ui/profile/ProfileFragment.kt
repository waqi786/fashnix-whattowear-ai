package com.fashnix.app.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fashnix.app.LocaleHelper
import com.fashnix.app.R
import com.fashnix.app.data.model.UserActivity
import com.fashnix.app.data.model.UserProfile
import com.fashnix.app.databinding.ItemActivityRowBinding
import com.fashnix.app.databinding.FragmentProfileBinding
import com.fashnix.app.ui.home.HomeViewModel
import com.fashnix.app.util.NotificationAgent
import com.fashnix.app.util.SafeImageLoader
import com.fashnix.app.util.addExpertHoverEffect
import com.fashnix.app.util.animateEntrance
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val profilePhotoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { saveProfilePhoto(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLuxuryInteractions()
        setupStateObservation()
        setupActionListeners()
        setupActivityRecycler()
        updateLanguageLabel(getSavedLanguageCode())
        executeCinematicAnimations()
        
        viewModel.loadProfile()
    }

    private fun setupLuxuryInteractions() {
        binding.profileHeaderCard.addExpertHoverEffect()
        binding.profileAvatarFrame.addExpertHoverEffect()
        binding.profileAvatar.addExpertHoverEffect()
        binding.languageCard.addExpertHoverEffect()
        binding.logoutButton.addExpertHoverEffect()
        binding.editProfileBtn.addExpertHoverEffect()
    }

    private fun setupStateObservation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    renderUiState(state)
                }
            }
        }

        homeViewModel.recentActivity.observe(viewLifecycleOwner) { activities ->
            (binding.activityRecycler.adapter as? ActivityAdapter)?.submitList(activities)
        }
    }

    private fun renderUiState(state: ProfileUiState) {
        state.userProfile?.let { displayIdentity(it) }
        binding.photoUploadProgress.visibility = if (state.isPhotoUploading) View.VISIBLE else View.GONE
        binding.avatarEditBadge.visibility = if (state.isPhotoUploading) View.INVISIBLE else View.VISIBLE

        binding.vaultWorth.text = state.stylingCount.toString()
        
        binding.utilizationScore.text = "${state.deploymentRate}%"
        binding.prestigeProgress.setProgress(state.influencePoints % 100, true)
        binding.influenceCount.text = String.format(Locale.US, "%.1fk", state.influencePoints / 1000.0)
        
        binding.apparelProgress.setProgress(state.apparelPercent, true)
        binding.apparelPercentText.text = "${state.apparelPercent}%"
        
        binding.footwearProgress.setProgress(state.footwearPercent, true)
        binding.footwearPercentText.text = "${state.footwearPercent}%"
        
        binding.accessoriesProgress.setProgress(state.accessoriesPercent, true)
        binding.accessoriesPercentText.text = "${state.accessoriesPercent}%"
        
        binding.stylingCount.text = state.stylingCount.toString()
        binding.trophyCount.text = state.trophyCount.toString()

        if (state.errorMessage != null) {
            Toast.makeText(requireContext(), "SYSTEM_ALERT: ${state.errorMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun displayIdentity(profile: UserProfile) {
        binding.apply {
            profileName.text = safeProfileName(profile.name)
            memberTierText.text = "${profile.memberTier.uppercase()} STATUS"
            
            styleDNADesc.text = profile.styleDNA.ifEmpty { getString(R.string.style_dna_desc) }

            SafeImageLoader.loadProfileImage(profileAvatar, profile.photoUrl)
        }
    }

    private fun setupActivityRecycler() {
        binding.activityRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ActivityAdapter()
            isNestedScrollingEnabled = false
        }
    }

    private fun setupActionListeners() {
        binding.profileToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val openPhotoPicker = View.OnClickListener {
            profilePhotoPicker.launch("image/*")
        }
        binding.profileAvatarFrame.setOnClickListener(openPhotoPicker)
        binding.profileAvatar.setOnClickListener(openPhotoPicker)
        binding.avatarEditBadge.setOnClickListener(openPhotoPicker)

        binding.languageCard.setOnClickListener {
            showDialectSelectionDialog()
        }

        binding.logoutButton.setOnClickListener { initiateLogoutProtocol() }
        
        binding.editProfileBtn.setOnClickListener {
            NotificationAgent.sendStyleNotification(requireContext(), "System Update", "Redirecting to Style DNA Calibration...")
            findNavController().navigate(R.id.styleQuizFragment)
        }
    }

    private fun showDialectSelectionDialog() {
        val dialects = arrayOf("English", "Hindi", "Urdu", "Spanish", "French")
        val codes = arrayOf("en", "hi", "ur", "es", "fr")
        
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Fashnix_Dialog)
            .setTitle("Select language")
            .setItems(dialects) { _, which ->
                applyDialectChange(codes[which])
            }
            .show()
    }

    private fun applyDialectChange(code: String) {
        requireContext()
            .getSharedPreferences("fashnix_prefs_fast", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("selected_language", code)
            .apply()
        viewModel.saveLanguage(code)
        LocaleHelper.applyLocale(requireContext(), code)
        updateLanguageLabel(code)
        Toast.makeText(requireContext(), "Language changed to ${languageLabel(code)}", Toast.LENGTH_SHORT).show()
        requireActivity().recreate()
    }

    private fun getSavedLanguageCode(): String {
        return requireContext()
            .getSharedPreferences("fashnix_prefs_fast", android.content.Context.MODE_PRIVATE)
            .getString("selected_language", "en") ?: "en"
    }

    private fun updateLanguageLabel(code: String) {
        binding.currentLanguage.text = languageLabel(code).uppercase(Locale.getDefault())
    }

    private fun languageLabel(code: String): String = when (code) {
        "hi" -> "Hindi"
        "ur" -> "Urdu"
        "es" -> "Spanish"
        "fr" -> "French"
        else -> "English"
    }

    private fun saveProfilePhoto(uri: Uri) {
        SafeImageLoader.loadProfileImage(binding.profileAvatar, uri.toString())
        val bytes = prepareProfilePhotoBytes(uri)

        if (bytes == null) {
            Toast.makeText(requireContext(), "Could not read selected photo", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "Uploading profile photo...", Toast.LENGTH_SHORT).show()
        viewModel.uploadProfilePhoto(bytes)
    }

    private fun prepareProfilePhotoBytes(uri: Uri): ByteArray? {
        return runCatching {
            val resolver = requireContext().contentResolver
            val bitmap = resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return@runCatching null

            val maxSide = 1024f
            val scale = minOf(1f, maxSide / maxOf(bitmap.width, bitmap.height).toFloat())
            val uploadBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                bitmap
            }

            ByteArrayOutputStream().use { output ->
                uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 86, output)
                output.toByteArray()
            }.also {
                if (uploadBitmap !== bitmap) uploadBitmap.recycle()
                bitmap.recycle()
            }
        }.getOrNull()
    }

    private fun safeProfileName(name: String): String {
        val normalized = name.lowercase(Locale.getDefault())
        return if (name.isBlank() || "waqar" in normalized || "ali" in normalized) {
            "STYLE PROFILE"
        } else {
            name.uppercase(Locale.getDefault())
        }
    }

    private fun initiateLogoutProtocol() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Fashnix_Dialog)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.logout()
                requireActivity().finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun executeCinematicAnimations() {
        val sequence = listOf(
            binding.profileHeaderCard,
            binding.vaultWorth,
            binding.utilizationScore,
            binding.languageCard,
            binding.logoutButton
        )
        sequence.forEachIndexed { i, v -> v.animateEntrance(i) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ActivityAdapter : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {
        private var items = listOf<UserActivity>()

        fun submitList(newItems: List<UserActivity>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemActivityRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.apply {
                activityTitle.text = item.title.uppercase()
                activityDesc.text = item.description
                activityTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.timestamp))
            }
        }

        override fun getItemCount() = items.size
        inner class ViewHolder(val binding: ItemActivityRowBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
