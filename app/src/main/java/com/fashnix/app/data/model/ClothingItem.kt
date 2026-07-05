package com.fashnix.app.data.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class ClothingItem(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val color: String = "",
    val occasion: String = "",
    val gender: String = "",
    val pattern: String = "",
    val name: String = "",
    val brand: String = "",
    val price: Double = 0.0,
    val wearCount: Int = 0,
    val lastWorn: Long = 0L,
    val laundryStatus: String = "Clean",
    val lastLaundryDate: Long = 0L,
    val laundryIntervalWears: Int = 3,
    val isFavorite: Boolean = false,
    val sleeveLength: String? = null,
    val isBorrowed: Boolean = false,
    val borrowedFrom: String? = null,
    val borrowedFromUserId: String? = null,
    val returnDate: Long? = null
) : Parcelable
