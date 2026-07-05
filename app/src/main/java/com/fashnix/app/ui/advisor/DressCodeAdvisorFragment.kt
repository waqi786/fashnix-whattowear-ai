package com.fashnix.app.ui.advisor

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.data.repository.AuthRepository
import com.fashnix.app.data.repository.WardrobeRepository
import com.fashnix.app.databinding.FragmentDressCodeAdvisorBinding
import com.fashnix.app.databinding.ItemClothingCardBinding
import com.fashnix.app.domain.DressCodeRules
import com.fashnix.app.util.addExpertHoverEffect
import com.fashnix.app.util.SafeImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

@AndroidEntryPoint
class DressCodeAdvisorFragment : Fragment() {

    private var _binding: FragmentDressCodeAdvisorBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var wardrobeRepository: WardrobeRepository
    private var isEventPickerShowing = false

    // Intelligent Event Mapping for better "Responsiveness"
    private val eventMap = mapOf(
        "Job Interview" to listOf("Professional", "Formal"),
        "Cocktail Party" to listOf("Festive", "Formal"),
        "Beach Wedding" to listOf("Casual", "Festive"),
        "Festive Ethnic" to listOf("Festive"),
        "Black Tie Gala" to listOf("Formal"),
        "First Date" to listOf("Casual", "Professional"),
        "Casual Friday" to listOf("Casual"),
        "Sports Event" to listOf("Sports"),
        "Business Casual" to listOf("Professional")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDressCodeAdvisorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        binding.advisorToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupUI() {
        val adapter = ArrayAdapter(requireContext(), R.layout.layout_dropdown_item, eventMap.keys.toList())
        binding.eventAutoComplete.setAdapter(adapter)
        binding.eventAutoComplete.threshold = 0
        binding.eventAutoComplete.setText(eventMap.keys.first(), false)
        binding.eventAutoComplete.setOnClickListener { showEventPicker() }
        binding.eventAutoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.eventAutoComplete.post { showEventPicker() }
        }

        binding.getAdviceButton.setOnClickListener {
            val selectedEvent = binding.eventAutoComplete.text.toString()
                .takeIf { it.isNotBlank() && eventMap.containsKey(it) }
                ?: eventMap.keys.first()
            binding.eventAutoComplete.setText(selectedEvent, false)
            generateIntelligenceReport(selectedEvent)
        }

        binding.getAdviceButton.addExpertHoverEffect()
        binding.planOutfitButton.addExpertHoverEffect()
        binding.planOutfitButton.setOnClickListener {
            findNavController().navigate(R.id.plannerFragment)
        }
    }

    private fun showEventPicker() {
        if (!isAdded || isEventPickerShowing) return
        isEventPickerShowing = true
        val events = eventMap.keys.toTypedArray()
        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Fashnix_Dialog)
            .setTitle("Select Event")
            .setItems(events) { _, which ->
                binding.eventAutoComplete.setText(events[which], false)
                binding.eventAutoComplete.clearFocus()
            }
            .setOnDismissListener {
                isEventPickerShowing = false
                binding.eventAutoComplete.clearFocus()
            }
            .show()
    }

    private fun generateIntelligenceReport(event: String) {
        lifecycleScope.launch {
            binding.getAdviceButton.isEnabled = false
            
            // 1. Fetch AI Guidance
            val guideText = DressCodeRules.getFallbackText(event)
            binding.guideText.text = guideText
            binding.guideText.movementMethod = ScrollingMovementMethod()

            // 2. Intelligent Matching from Vault
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                Toast.makeText(requireContext(), "Please sign in to use wardrobe advice.", Toast.LENGTH_LONG).show()
                binding.getAdviceButton.isEnabled = true
                return@launch
            }
            val wardrobe = wardrobeRepository.getUserItems(userId).getOrDefault(emptyList())
            
            val mappedTags = eventMap[event] ?: listOf(event)
            val matchedItems = wardrobe.filter { item ->
                mappedTags.any { tag -> item.occasion.contains(tag, true) || item.category.contains(tag, true) }
            }

            // 3. Update UI
            binding.resultsContainer.visibility = View.VISIBLE
            binding.resultsContainer.alpha = 0f
            binding.resultsContainer.animate().alpha(1f).setDuration(500).start()

            if (matchedItems.isNotEmpty()) {
                binding.wardrobeGrid.adapter = MatchedItemAdapter(matchedItems)
                binding.noItemsText.visibility = View.GONE
            } else {
                binding.wardrobeGrid.adapter = MatchedItemAdapter(emptyList())
                binding.noItemsText.visibility = View.VISIBLE
            }
            
            binding.getAdviceButton.isEnabled = true
        }
    }

    inner class MatchedItemAdapter(private val items: List<ClothingItem>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<MatchedItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemClothingCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.clothingName.text = item.name.uppercase()
            holder.binding.clothingCategory.text = item.category
            SafeImageLoader.loadWardrobeImage(holder.binding.clothingImage, item.imageUrl)
        }

        override fun getItemCount() = items.size
        inner class ViewHolder(val binding: ItemClothingCardBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
