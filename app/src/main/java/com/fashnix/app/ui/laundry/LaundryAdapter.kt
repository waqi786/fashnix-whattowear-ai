package com.fashnix.app.ui.laundry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.databinding.ItemLaundryCardBinding
import com.fashnix.app.util.SafeImageLoader
import com.fashnix.app.util.addExpertHoverEffect

class LaundryAdapter(private val onMarkClean: (ClothingItem) -> Unit) :
    ListAdapter<ClothingItem, LaundryAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<ClothingItem>() {
        override fun areItemsTheSame(oldItem: ClothingItem, newItem: ClothingItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ClothingItem, newItem: ClothingItem) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLaundryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemLaundryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.addExpertHoverEffect()
            binding.markCleanButton.addExpertHoverEffect()
        }

        fun bind(item: ClothingItem) {
            binding.apply {
                laundryItemName.text = item.name
                laundryItemCategory.text = item.category.uppercase()
                laundryStatusChip.text = if (item.laundryStatus.equals("Dirty", true)) {
                    "DIRTY"
                } else {
                    "NEEDS WASH"
                }
                
                wornSinceWash.text = "Worn ${item.wearCount} times"
                
                SafeImageLoader.loadWardrobeImage(laundryImage, item.imageUrl)

                markCleanButton.setOnClickListener { onMarkClean(item) }
            }
        }
    }
}
