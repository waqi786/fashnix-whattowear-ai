package com.fashnix.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fashnix.app.R
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.databinding.ItemOutfitCardBinding
import com.fashnix.app.util.SafeImageLoader
import com.fashnix.app.util.addExpertHoverEffect

class OutfitPagerAdapter(
    private var items: List<ClothingItem>,
    private val onClick: (ClothingItem) -> Unit
) : RecyclerView.Adapter<OutfitPagerAdapter.ViewHolder>() {

    fun updateItems(newItems: List<ClothingItem>) {
        val oldItems = items
        items = newItems
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldItems.size
            override fun getNewListSize(): Int = items.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition].id == items[newItemPosition].id
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldItems[oldItemPosition] == items[newItemPosition]
        })
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOutfitCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemOutfitCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.addExpertHoverEffect()
            binding.favoriteButton.addExpertHoverEffect()
        }

        fun bind(item: ClothingItem) {
            binding.apply {
                itemName.text = item.name
                itemBrand.text = item.category.ifEmpty { "WARDROBE" }.uppercase()
                itemCategory.text = item.occasion.uppercase()
                itemPrice.text = "${item.wearCount.coerceAtLeast(1)} WEARS"

                SafeImageLoader.loadWardrobeImage(
                    itemImage,
                    item.imageUrl,
                    fallbackRes = fallbackFor(item)
                )

                root.setOnClickListener { onClick(item) }
                
                favoriteButton.isSelected = item.isFavorite
                favoriteButton.setOnClickListener {
                    favoriteButton.isSelected = !favoriteButton.isSelected
                }
            }
        }

        private fun fallbackFor(item: ClothingItem): Int {
            return when {
                item.category.equals("Footwear", ignoreCase = true) -> R.drawable.sample_footwear_card
                item.category.equals("Accessories", ignoreCase = true) -> R.drawable.sample_accessory_card
                else -> R.drawable.sample_apparel_card
            }
        }
    }
}
