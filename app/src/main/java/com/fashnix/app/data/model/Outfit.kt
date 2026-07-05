package com.fashnix.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Outfit(
    val id: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val name: String = "",
    val items: List<ClothingItem> = emptyList(),
    val mood: String = "",
    val weatherWarning: String = "",
    val rating: Int = 0,
    val isWorn: Boolean = false
) : Parcelable