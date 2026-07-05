package com.fashnix.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Suggestion(
    val itemName: String = "",
    val color: String = "",
    val type: String = "",
    val reason: String = "",
    val fromWardrobe: Boolean = false,
    val wardrobeItemId: String? = null
) : Parcelable