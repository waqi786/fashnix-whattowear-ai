package com.fashnix.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccessorySet(
    val shoes: List<Suggestion> = emptyList(),
    val belt: List<Suggestion> = emptyList(),
    val watch: List<Suggestion> = emptyList(),
    val tie: List<Suggestion> = emptyList(),
    val pocketSquare: List<Suggestion> = emptyList(),
    val jewelry: List<Suggestion> = emptyList(),
    val bag: List<Suggestion> = emptyList(),
    val sunglasses: List<Suggestion> = emptyList()
) : Parcelable