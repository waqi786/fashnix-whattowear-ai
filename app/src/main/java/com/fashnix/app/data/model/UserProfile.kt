package com.fashnix.app.data.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

/**
 * UserProfile: Enterprise-grade user data model for Fashnix Luxe.
 * Stores comprehensive user identity and behavioral metrics.
 */
@IgnoreExtraProperties
@Parcelize
data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val gender: String = "",
    val bodyType: String = "",
    val skinTone: String = "",
    val points: Int = 0, // Experience points for gamification
    val preferences: Map<String, String> = emptyMap(),
    val stylePreference: String = "",
    val styleDNA: String = "",
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val lastStreakDate: Long = 0L,
    val badges: List<String> = emptyList(),
    val familyGroupId: String? = null,
    val fcmToken: String? = null,
    val selectedLanguage: String = "en",
    val memberTier: String = "SILVER", // SILVER, GOLD, PLATINUM, ELITE
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
