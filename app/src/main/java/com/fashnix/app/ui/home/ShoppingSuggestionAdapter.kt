package com.fashnix.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fashnix.app.R
import com.fashnix.app.data.model.ShoppingSuggestion
import com.fashnix.app.databinding.ItemSuggestionCardBinding
import com.fashnix.app.util.addExpertHoverEffect

class ShoppingSuggestionAdapter(
    private val onClick: (ShoppingSuggestion) -> Unit
) : ListAdapter<ShoppingSuggestion, ShoppingSuggestionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuggestionCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSuggestionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.addExpertHoverEffect()
        }

        fun bind(item: ShoppingSuggestion) {
            binding.apply {
                suggestionTitle.text = item.title
                suggestionBrand.text = item.brand.uppercase()
                suggestionPrice.text = item.price
                suggestionReason.text = item.reason

                // Expert Fix: Secure image loading with failover
                Glide.with(itemView.context)
                    .load(item.externalUrl)
                    .placeholder(R.drawable.ic_wardrobe_placeholder)
                    .error(R.drawable.ic_wardrobe_placeholder)
                    .centerCrop()
                    .into(suggestionImage)

                root.setOnClickListener { onClick(item) }
                btnAction.setOnClickListener { onClick(item) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ShoppingSuggestion>() {
        override fun areItemsTheSame(oldItem: ShoppingSuggestion, newItem: ShoppingSuggestion) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShoppingSuggestion, newItem: ShoppingSuggestion) =
            oldItem == newItem
    }
}
