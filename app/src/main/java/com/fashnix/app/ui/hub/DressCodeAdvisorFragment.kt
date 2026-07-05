package com.fashnix.app.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentDressCodeAdvisorBinding
import com.fashnix.app.ui.home.HomeViewModel
import com.fashnix.app.ui.home.OutfitPagerAdapter
import com.fashnix.app.util.addExpertHoverEffect
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/**
 * DressCodeAdvisorFragment: AI-driven event styling consultant.
 */
@AndroidEntryPoint
class DressCodeAdvisorFragment : Fragment() {

    private var _binding: FragmentDressCodeAdvisorBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private val events = arrayOf("Formal", "Business", "Casual", "Festive", "Cocktail")
    private var isEventPickerShowing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDressCodeAdvisorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdvisorUI()
        observeAdvisorStream()
    }

    private fun setupAdvisorUI() {
        val adapter = ArrayAdapter(requireContext(), R.layout.layout_dropdown_item, events)
        binding.eventAutoComplete.setAdapter(adapter)
        binding.eventAutoComplete.threshold = 0
        binding.eventAutoComplete.setOnClickListener { showEventPicker() }
        binding.eventAutoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.eventAutoComplete.post { showEventPicker() }
        }

        binding.getAdviceButton.addExpertHoverEffect()
        binding.getAdviceButton.setOnClickListener {
            val selectedEvent = binding.eventAutoComplete.text.toString()
            if (selectedEvent.isNotEmpty()) {
                analyzeDressCode(selectedEvent)
            }
        }
        
        binding.advisorToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.planOutfitButton.setOnClickListener { findNavController().navigateUp() }
    }

    private fun showEventPicker() {
        if (!isAdded || isEventPickerShowing) return
        isEventPickerShowing = true
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

    private fun analyzeDressCode(occasion: String) {
        binding.resultsContainer.visibility = View.VISIBLE
        binding.guideText.text = when(occasion) {
            "Formal" -> "Elegance is not standing out, but being remembered. Stick to dark silhouettes and fine textures."
            "Business" -> "Structure creates authority. Focus on tailored shoulders and neutral tones for professional dominance."
            else -> "Comfort is the new luxury. Layer high-quality textures for a sophisticated yet effortless look."
        }
        
        viewModel.generateDailyOutfit()
    }

    private fun observeAdvisorStream() {
        viewModel.dailyRecommendations.observe(viewLifecycleOwner) { items ->
            if (!items.isNullOrEmpty()) {
                binding.wardrobeGrid.adapter = OutfitPagerAdapter(items) { /* Select */ }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
