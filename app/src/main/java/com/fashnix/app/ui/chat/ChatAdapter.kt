package com.fashnix.app.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.fashnix.app.R
import com.fashnix.app.data.model.ChatMessage
import com.fashnix.app.databinding.ItemChatMessageAiBinding
import com.fashnix.app.databinding.ItemChatMessageUserBinding
import com.fashnix.app.databinding.ItemChatTypingBinding
import com.fashnix.app.util.addExpertHoverEffect

/**
 * ChatAdapter: A high-performance, multi-view type adapter for the AI Stylist.
 * Engineered for smooth scrolling and elite visual fidelity.
 */
class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback) {

    private var isTyping = false

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
        private const val VIEW_TYPE_TYPING = 3

        val DiffCallback = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
        }
    }

    fun showTyping(typing: Boolean) {
        if (isTyping == typing) return
        isTyping = typing
        if (isTyping) {
            notifyItemInserted(itemCount)
        } else {
            notifyItemRemoved(itemCount - 1)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList.size) {
            if (getItem(position).role == "user") VIEW_TYPE_USER else VIEW_TYPE_AI
        } else VIEW_TYPE_TYPING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> UserViewHolder(ItemChatMessageUserBinding.inflate(inflater, parent, false))
            VIEW_TYPE_AI -> AiViewHolder(ItemChatMessageAiBinding.inflate(inflater, parent, false))
            else -> TypingViewHolder(ItemChatTypingBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> holder.bind(getItem(position))
            is AiViewHolder -> holder.bind(getItem(position))
            is TypingViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int = currentList.size + if (isTyping) 1 else 0

    inner class UserViewHolder(private val binding: ItemChatMessageUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.userMessageText.text = message.content
            
            // Handle Multi-modal Content (Images)
            if (!message.imageUrl.isNullOrEmpty()) {
                binding.userMessageImage.visibility = View.VISIBLE
                Glide.with(binding.userMessageImage)
                    .load(message.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(binding.userMessageImage)
            } else {
                binding.userMessageImage.visibility = View.GONE
            }

            binding.userMessageTime.text = "Just now"
            binding.root.addExpertHoverEffect()
        }
    }

    inner class AiViewHolder(private val binding: ItemChatMessageAiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.aiMessageText.text = message.content
            binding.aiMessageTime.text = "Fashnix AI Stylist"
            binding.root.addExpertHoverEffect()
        }
    }

    inner class TypingViewHolder(binding: ItemChatTypingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            // Lottie animation starts automatically via XML
        }
    }
}