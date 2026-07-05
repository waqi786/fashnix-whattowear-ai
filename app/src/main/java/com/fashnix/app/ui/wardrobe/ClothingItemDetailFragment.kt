package com.fashnix.app.ui.wardrobe

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fashnix.app.R
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.databinding.FragmentClothingDetailBinding
import com.fashnix.app.util.NotificationAgent
import com.fashnix.app.util.SafeImageLoader
import com.fashnix.app.util.addExpertHoverEffect
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/**
 * ClothingItemDetailFragment: The AI wardrobe item viewer.
 * Fully re-engineered for functional Edit/Delete operations and deep analytics.
 */
@AndroidEntryPoint
class ClothingItemDetailFragment : Fragment() {

    private var _binding: FragmentClothingDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WardrobeViewModel by viewModels()
    private val args: ClothingItemDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClothingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupNavigation()
        setupObservers()
        
        val itemId = args.clothingItemId
        if (itemId.isNotEmpty()) {
            viewModel.loadItem(itemId)
        }
    }

    private fun setupNavigation() {
        binding.detailToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.editButton.setOnClickListener {
            performHaptic(VibrationEffect.EFFECT_CLICK)
            val action = ClothingItemDetailFragmentDirections.actionClothingItemDetailFragmentToEditItemFragment(args.clothingItemId)
            findNavController().navigate(action)
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.markAsWornButton.setOnClickListener {
            viewModel.selectedItem.value?.let { item ->
                viewModel.markAsWorn(item)
                performHaptic(VibrationEffect.EFFECT_HEAVY_CLICK)
                Toast.makeText(requireContext(), getString(R.string.item_marked_worn), Toast.LENGTH_SHORT).show()
                NotificationAgent.sendStyleNotification(requireContext(), "Style Logged", "You looked great in ${item.name}!")
            }
        }
        
        binding.editButton.addExpertHoverEffect()
        binding.deleteButton.addExpertHoverEffect()
        binding.markAsWornButton.addExpertHoverEffect()
    }

    private fun setupObservers() {
        viewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            item?.let { renderDetails(it) }
        }
    }

    private fun renderDetails(item: ClothingItem) {
        binding.apply {
            detailName.text = item.name.uppercase()
            detailPrice.text = item.laundryStatus.uppercase()
            detailBrand.text = listOf(item.category, item.occasion)
                .filter { it.isNotBlank() }
                .joinToString(" / ")
                .ifBlank { "AI WARDROBE ITEM" }

            SafeImageLoader.loadWardrobeImage(detailImage, item.imageUrl)

            renderChips(item)
            updateAnalytics(item)
        }
    }

    private fun renderChips(item: ClothingItem) {
        binding.detailChipGroup.removeAllViews()
        val tags = listOf(item.category, item.color, item.occasion, item.gender)
        tags.filter { it.isNotEmpty() }.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag.uppercase()
                setChipBackgroundColorResource(R.color.surface_glass)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setChipStrokeWidthResource(R.dimen.chip_stroke_width)
                setChipStrokeColorResource(R.color.border_glass)
            }
            binding.detailChipGroup.addView(chip)
        }
    }

    private fun updateAnalytics(item: ClothingItem) {
        binding.rowBrand.apply {
            rowLabel.text = "CATEGORY"
            rowValue.text = item.category.ifEmpty { "WARDROBE" }.uppercase()
        }
        binding.rowCpw.apply {
            rowLabel.text = "WEAR COUNT"
            rowValue.text = item.wearCount.coerceAtLeast(0).toString()
        }
        binding.rowLastWorn.apply {
            rowLabel.text = getString(R.string.last_worn)
            rowValue.text = if (item.lastWorn > 0) "RECENTLY" else getString(R.string.never)
        }
        binding.rowSustainability.apply {
            rowLabel.text = "ECO"
            rowValue.text = "A+"
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Fashnix_Dialog)
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.selectedItem.value?.let { 
                    viewModel.deleteItem(it)
                    Toast.makeText(requireContext(), "ITEM REMOVED", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performHaptic(effect: Int) {
        val v = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            v.vibrate(VibrationEffect.createPredefined(effect))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
