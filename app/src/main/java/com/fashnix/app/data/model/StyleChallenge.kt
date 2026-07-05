package com.fashnix.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StyleChallenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val requiredStreak: Int = 0,
    val badgeIcon: String = "",
    val isCompleted: Boolean = false,
    val completedDate: Long? = null
) : Parcelable