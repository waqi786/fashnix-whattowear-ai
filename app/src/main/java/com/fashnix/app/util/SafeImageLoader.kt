package com.fashnix.app.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.fashnix.app.R

object SafeImageLoader {
    private val blockedRemotePlaceholders = listOf(
        "via.placeholder.com",
        "placeholder.com"
    )

    fun loadWardrobeImage(
        imageView: ImageView,
        imageUrl: String?,
        circleCrop: Boolean = false,
        fallbackRes: Int = R.drawable.ic_wardrobe_placeholder
    ) {
        val safeSource = imageUrl
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.takeUnless { url -> blockedRemotePlaceholders.any { url.contains(it, ignoreCase = true) } }
            ?.let { source ->
                when (source.lowercase()) {
                    "sample:apparel" -> R.drawable.sample_apparel_card
                    "sample:footwear" -> R.drawable.sample_footwear_card
                    "sample:accessory", "sample:accessories" -> R.drawable.sample_accessory_card
                    else -> source
                }
            }

        val request = Glide.with(imageView)
            .load(safeSource ?: fallbackRes)
            .placeholder(fallbackRes)
            .error(fallbackRes)
            .transition(DrawableTransitionOptions.withCrossFade())

        if (circleCrop) {
            request.circleCrop().into(imageView)
        } else {
            request
                .apply(RequestOptions().transform(CenterCrop()))
                .into(imageView)
        }
    }

    fun loadProfileImage(imageView: ImageView, photoUrl: String?) {
        val safeSource = photoUrl
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.takeUnless { url -> blockedRemotePlaceholders.any { url.contains(it, ignoreCase = true) } }

        Glide.with(imageView)
            .load(safeSource ?: R.drawable.ic_profile_photo_placeholder)
            .placeholder(R.drawable.ic_profile_photo_placeholder)
            .error(R.drawable.ic_profile_photo_placeholder)
            .circleCrop()
            .into(imageView)
    }
}
