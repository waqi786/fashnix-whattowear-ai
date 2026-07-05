package com.fashnix.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Enterprise-Grade Data Models for the Fashnix Luxe Ecosystem.
 * Unified model repository to prevent build ambiguities.
 */

@Parcelize
data class CommunityPost(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val imageUrl: String = "",
    val likes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class ShoppingSuggestion(
    val id: String = "",
    val title: String = "",
    val brand: String = "",
    val price: String = "",
    val reason: String = "",
    val externalUrl: String = ""
) : Parcelable

@Parcelize
data class PrestigeRank(
    val id: String = "",
    val name: String = "",
    val points: Int = 0,
    val tier: String = "VISIONARY",
    val avatarUrl: String = ""
) : Parcelable

@Parcelize
data class StyleEvent(
    val id: String = "",
    val title: String = "",
    val dressCode: String = "",
    val date: Long = 0L,
    val location: String = ""
) : Parcelable

@Parcelize
data class EvolutionPoint(
    val month: String = "",
    val score: Double = 0.0
) : Parcelable

@Parcelize
data class DailyForecastPlan(
    val day: String = "",
    val condition: String = "",
    val advice: String = "",
    val temperature: Int = 22
) : Parcelable

@Parcelize
data class UserActivity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "DEPLOYMENT", // DEPLOYMENT, INGESTION, ACHIEVEMENT
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
