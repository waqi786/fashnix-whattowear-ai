package com.fashnix.app.ui.planner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fashnix.app.R
import com.fashnix.app.data.model.Outfit
import com.fashnix.app.databinding.ItemPlannedOutfitBinding
import com.fashnix.app.util.addExpertHoverEffect
import com.google.android.material.chip.Chip

class OutfitAdapter(
    private val onMarkWorn: (Outfit) -> Unit,
    private val onDelete: (Outfit) -> Unit
) : ListAdapter<Outfit, OutfitAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Outfit>() {
        override fun areItemsTheSame(oldItem: Outfit, newItem: Outfit) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Outfit, newItem: Outfit) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlannedOutfitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPlannedOutfitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Expert Touch: Hover effect for planned looks
            binding.root.addExpertHoverEffect()
        }

        fun bind(outfit: Outfit) {
            binding.apply {
                outfitName.text = outfit.name
                moodChip.text = outfit.mood.uppercase()
                
                // Expert Chip Styling
                itemChips.removeAllViews()
                outfit.items.take(3).forEach { item ->
                    val chip = Chip(itemChips.context).apply {
                        text = item.name
                        setChipBackgroundColorResource(R.color.surface_glass)
                        setTextColor(context.getColor(R.color.text_secondary))
                        textSize = 10f
                    }
                    itemChips.addView(chip)
                }

                if (outfit.items.size > 3) {
                    val moreChip = Chip(itemChips.context).apply {
                        text = "+${outfit.items.size - 3}"
                        setTextColor(context.getColor(R.color.secondary))
                    }
                    itemChips.addView(moreChip)
                }

                markWornButton.setOnClickListener { onMarkWorn(outfit) }
                btnOptions.setOnClickListener { onDelete(outfit) }
            }
        }
    }
}