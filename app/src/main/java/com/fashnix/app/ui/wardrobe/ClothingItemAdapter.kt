package com.fashnix.app.ui.wardrobe

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fashnix.app.R
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.databinding.ItemClothingGridBinding
import com.fashnix.app.util.SafeImageLoader
import com.fashnix.app.util.addExpertHoverEffect

class ClothingItemAdapter(
    private val onItemClick: (ClothingItem) -> Unit,
    private val onLongClick: ((ClothingItem) -> Unit)? = null
) : ListAdapter<ClothingItem, ClothingItemAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ClothingItem>() {
        override fun areItemsTheSame(oldItem: ClothingItem, newItem: ClothingItem): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ClothingItem, newItem: ClothingItem): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClothingGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemClothingGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.addExpertHoverEffect()
            
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLongClick?.invoke(getItem(position))
                }
                true
            }
        }

        fun bind(item: ClothingItem) {
            binding.apply {
                itemName.text = item.name
                itemPrice.text = item.occasion.ifBlank { item.laundryStatus }.uppercase()

                SafeImageLoader.loadWardrobeImage(clothingImage, item.imageUrl)

                val dotColor = try { Color.parseColor(item.color) } catch (e: Exception) { Color.GRAY }
                (colorIndicator.background as? GradientDrawable)?.setColor(dotColor)

                val laundryTint = when (item.laundryStatus) {
                    "Clean" -> ContextCompat.getColor(root.context, R.color.primary)
                    "NeedsWash" -> ContextCompat.getColor(root.context, R.color.warning)
                    else -> ContextCompat.getColor(root.context, R.color.error)
                }
                laundryStatusIcon.setColorFilter(laundryTint)
            }
        }
    }
}
