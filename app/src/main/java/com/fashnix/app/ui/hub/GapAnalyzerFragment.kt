package com.fashnix.app.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fashnix.app.R
import com.fashnix.app.databinding.FragmentGapAnalyzerBinding
import com.fashnix.app.databinding.ItemSuggestionCardBinding
import com.fashnix.app.util.addExpertHoverEffect
import dagger.hilt.android.AndroidEntryPoint

/**
 * GapAnalyzerFragment: Executive Wardrobe Auditing System.
 * Detects useful missing wardrobe items and gives practical closet suggestions.
 */
@AndroidEntryPoint
class GapAnalyzerFragment : Fragment() {

    private var _binding: FragmentGapAnalyzerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGapAnalyzerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupExpertUI()
        runEntranceAnimations()
        
        binding.gapToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupExpertUI() {
        // High-end gap data mapping the user's style evolution
        val gaps = listOf(
            GapData("AI GAP", "Structured Blazer", "NEEDS MORE LAYERING", R.drawable.ic_suit, "ADD LAYER"),
            GapData("AI GAP", "Minimal Sneakers", "CASUAL LOOKS NEED SHOES", R.drawable.ic_hanger, "ADD SHOES"),
            GapData("AI GAP", "Clean Watch", "FORMAL ACCENT MISSING", R.drawable.ic_trophy, "ADD ACCENT")
        )

        binding.gapRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = GapAdapter(gaps)
            // Optimization for smooth scrolling
            setHasFixedSize(true)
        }
    }

    private fun runEntranceAnimations() {
        // Luxury Staggered Reveal
        binding.gapRecycler.alpha = 0f
        binding.gapRecycler.translationY = 100f
        binding.gapRecycler.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class GapData(
        val brand: String,
        val title: String, 
        val reason: String, 
        val iconRes: Int,
        val action: String
    )

    inner class GapAdapter(private val gaps: List<GapData>) :
        RecyclerView.Adapter<GapAdapter.GapViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GapViewHolder {
            val binding = ItemSuggestionCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return GapViewHolder(binding)
        }

        override fun onBindViewHolder(holder: GapViewHolder, position: Int) {
            holder.bind(gaps[position])
        }

        override fun getItemCount() = gaps.size

        inner class GapViewHolder(private val itemBinding: ItemSuggestionCardBinding) : 
            RecyclerView.ViewHolder(itemBinding.root) {
            
            fun bind(gap: GapData) {
                itemBinding.apply {
                    suggestionBrand.text = gap.brand
                    suggestionTitle.text = gap.title
                    suggestionReason.text = gap.reason
                    suggestionPrice.text = gap.action
                    
                    // Expert Fix: Fallback for all images to ensure visual continuity
                    Glide.with(itemView.context)
                        .load(gap.iconRes)
                        .placeholder(R.drawable.ic_wardrobe_placeholder)
                        .error(R.drawable.ic_wardrobe_placeholder)
                        .centerCrop()
                        .into(suggestionImage)
                    
                    // Professional Tactile Layer
                    root.addExpertHoverEffect()
                    
                    btnAction.setOnClickListener {
                        // Routing to procurement or marketplace
                    }
                }
            }
        }
    }
}
