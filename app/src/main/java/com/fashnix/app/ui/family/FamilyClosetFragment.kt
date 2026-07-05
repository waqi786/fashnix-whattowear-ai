package com.fashnix.app.ui.family

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fashnix.app.databinding.FragmentFamilyClosetBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FamilyClosetFragment : Fragment() {

    private var _binding: FragmentFamilyClosetBinding? = null
    private val binding get() = _binding!!
    private val presentationInviteCode = "FX-7421"
    // Assuming a ViewModel exists, if not we'll use a placeholder logic for UI demonstration
    // private val viewModel: FamilyViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFamilyClosetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExpertUI()
        runEntranceAnimations()
        
        binding.closetToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupExpertUI() {
        // High-end touch interactions
        binding.createGroupButton.addExpertHoverEffect()
        binding.joinGroupButton.addExpertHoverEffect()
        binding.familyOutfitButton.addExpertHoverEffect()
        binding.inviteMemberButton.addExpertHoverEffect()
        
        // Example logic for UI state toggle (demonstrating expert logic flow)
        binding.createGroupButton.setOnClickListener {
            showSharedCloset()
        }

        binding.joinGroupButton.setOnClickListener {
            showSharedCloset()
            Toast.makeText(requireContext(), "Shared closet joined", Toast.LENGTH_SHORT).show()
        }

        binding.inviteMemberButton.setOnClickListener {
            shareInviteCode()
        }
    }

    private fun showSharedCloset() {
        binding.inviteCodeText.text = "INVITE CODE  $presentationInviteCode"
        binding.noGroupState.animate().alpha(0f).setDuration(260).withEndAction {
            binding.noGroupState.visibility = View.GONE
            binding.groupState.visibility = View.VISIBLE
            binding.familyOutfitButton.visibility = View.VISIBLE
            binding.groupState.alpha = 0f
            binding.familyOutfitButton.alpha = 0f
            binding.groupState.animate().alpha(1f).setDuration(420).start()
            binding.familyOutfitButton.animate().alpha(1f).setDuration(420).start()
            runEntranceAnimations()
        }.start()
    }

    private fun shareInviteCode() {
        val shareText = "Join my Fashnix shared closet with code $presentationInviteCode. You can preview outfits and vote on looks."
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Fashnix invite", presentationInviteCode))
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }, "Share Fashnix closet"))
    }

    private fun runEntranceAnimations() {
        // Luxury staggered reveal for the Closet Vault elements
        val views = if (binding.noGroupState.visibility == View.VISIBLE) {
            listOf(binding.noGroupState.getChildAt(0))
        } else {
            listOf(binding.groupName, binding.memberCount, binding.memberList, binding.familyOutfitButton)
        }
        
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 60f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(100L + (index * 120L))
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
