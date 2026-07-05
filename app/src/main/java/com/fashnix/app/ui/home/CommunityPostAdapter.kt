package com.fashnix.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fashnix.app.R
import com.fashnix.app.data.model.CommunityPost
import com.fashnix.app.databinding.ItemCommunityPostBinding
import com.fashnix.app.util.addExpertHoverEffect

/**
 * CommunityPostAdapter: Orchestrates the global fashion feed with Billion-Dollar UX patterns.
 */
class CommunityPostAdapter(
    private val onPostClick: (CommunityPost) -> Unit
) : ListAdapter<CommunityPost, CommunityPostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, index: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCommunityPostBinding.inflate(layoutInflater, parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemCommunityPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.addExpertHoverEffect()
        }

        fun bind(post: CommunityPost) {
            binding.apply {
                postTitle.text = post.title.uppercase()
                postAuthor.text = "BY ${post.author.uppercase()}"
                likeCount.text = "${post.likes} STYLE CREDITS"

                // Expert Fix: Use itemView.context to avoid ambiguous root reference
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .placeholder(R.drawable.ic_wardrobe_placeholder)
                    .error(R.drawable.ic_wardrobe_placeholder)
                    .centerCrop()
                    .into(postImage)

                root.setOnClickListener { onPostClick(post) }
                
                favoriteBtn.setOnClickListener {
                    it.isSelected = !it.isSelected
                }
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<CommunityPost>() {
        override fun areItemsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
            return oldItem == newItem
        }
    }
}
