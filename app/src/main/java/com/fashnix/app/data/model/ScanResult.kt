package com.fashnix.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScanResult(
    val scanId: String = "",
    val category: String = "",
    val colour: String = "",
    val occasion: String = "",
    val gender: String = "",
    val pattern: String = "",
    val categoryConfidence: Float = 0f,
    val colourConfidence: Float = 0f,
    val occasionConfidence: Float = 0f,
    val genderConfidence: Float = 0f,
    val isColourOther: Boolean = false,
    val userSelectedColour: String? = null,
    val stylingTips: List<String> = emptyList(),
    val imageUrl: String = "",
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
