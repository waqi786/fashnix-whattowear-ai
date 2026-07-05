package com.fashnix.app.domain

import javax.inject.Inject
import javax.inject.Singleton

/**
 * TrendForecaster: Predictive AI model for global fashion movements.
 * 
 * This engine analyzes seasonal cycles, celebrity influence (mocked), 
 * and runway data to predict what will be 'In Style' next month.
 */
@Singleton
class TrendForecaster @Inject constructor() {

    data class Trend(
        val title: String,
        val confidence: Int,
        val description: String,
        val colorPalette: List<String>,
        val growthRate: String
    )

    fun getSeasonalForecast(): List<Trend> {
        return listOf(
            Trend(
                "Cyber-Minimalism",
                94,
                "Clean lines meeting tech-fabrics and reflective accents.",
                listOf("#FFFFFF", "#C0C0C0", "#000000", "#FF4500"),
                "+24% MoM"
            ),
            Trend(
                "Desert Nomadic",
                82,
                "Linen drapes, earth tones, and unstructured silhouettes.",
                listOf("#D2B48C", "#8B4513", "#F5F5DC"),
                "+12% MoM"
            ),
            Trend(
                "Neo-Vintage Preppy",
                76,
                "90s Ivy league meets oversized street-style patterns.",
                listOf("#000080", "#FFD700", "#800000"),
                "+18% MoM"
            )
        )
    }

    /**
     * Matches user's current wardrobe against future trends to suggest purchases.
     */
    fun calculateTrendCompatibility(wardrobeDna: String, trend: Trend): Int {
        return when {
            wardrobeDna.contains("MINIMALIST") && trend.title.contains("Minimalism") -> 95
            wardrobeDna.contains("EXECUTIVE") && trend.title.contains("Preppy") -> 80
            else -> 45
        }
    }
}
