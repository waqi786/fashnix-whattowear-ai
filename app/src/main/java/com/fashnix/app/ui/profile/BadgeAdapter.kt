package com.fashnix.app.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fashnix.app.R
import com.fashnix.app.databinding.ItemBadgeCardBinding
import com.fashnix.app.util.addExpertHoverEffect

class BadgeAdapter(private val earnedBadges: List<String>) :
    RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

    private val allBadges = listOf(
        Triple("7_day_streaker", "7-Day Streaker", R.drawable.ic_fire),
        Triple("14_day_streaker", "Style Legend", R.drawable.ic_sparkle),
        Triple("fashion_adventurer", "Trend Setter", R.drawable.ic_globe),
        Triple("mix_match_master", "Mix Master", R.drawable.ic_shuffle),
        Triple("family_stylist", "Closet Boss", R.drawable.ic_hub),
        Triple("laundry_hero", "Fabric Expert", R.drawable.ic_laundry_clean)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBadgeCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val badge = allBadges[position]
        val isEarned = earnedBadges.contains(badge.first)
        holder.bind(badge.second, badge.third, isEarned)
    }

    override fun getItemCount() = allBadges.size

    inner class ViewHolder(private val binding: ItemBadgeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            // Expert Touch: Interactive feedback on achievements
            binding.root.addExpertHoverEffect()
        }

        fun bind(name: String, iconRes: Int, isEarned: Boolean) {
            binding.badgeName.text = name.uppercase()
            binding.badgeIcon.setImageResource(iconRes)
            
            if (isEarned) {
                binding.root.alpha = 1.0f
                binding.badgeIcon.alpha = 1.0f
                binding.badgeIcon.setColorFilter(binding.root.context.getColor(R.color.secondary))
            } else {
                binding.root.alpha = 0.4f
                binding.badgeIcon.alpha = 0.3f
                binding.badgeIcon.setColorFilter(binding.root.context.getColor(R.color.text_secondary))
            }
        }
    }
}