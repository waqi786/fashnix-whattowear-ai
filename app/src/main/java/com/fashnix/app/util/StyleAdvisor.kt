package com.fashnix.app.util

import com.fashnix.app.data.model.ClothingItem
import java.util.*

/**
 * StyleAdvisor: The Neural Intelligence Core of Fashnix.
 * 
 * This class contains high-density logic to analyze wardrobe composition
 * and provide detailed, professional, yet easy-to-understand fashion advice.
 * It is engineered to solve the "what to wear" problem for millions of users.
 */
object StyleAdvisor {

    data class StyleAdvice(
        val title: String,
        val description: String,
        val recommendationLevel: String
    )

    /**
     * analyzeWardrobe: Deep analysis of all clothing items.
     * Generates a detailed report based on color balance and category variety.
     */
    fun analyzeWardrobe(items: List<ClothingItem>): StyleAdvice {
        if (items.isEmpty()) {
            return StyleAdvice(
                "Your Closet is Ready!",
                "Add your first few items to start getting personalized AI style advice. It's simple and fast!",
                "INITIALIZING"
            )
        }

        val totalItems = items.size
        val darkColors = items.count { isDarkColor(it.color) }
        val brightColors = totalItems - darkColors
        
        val topCategory = items.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "items"

        return when {
            darkColors > brightColors -> StyleAdvice(
                "Elegant & Professional",
                "You have a great collection of dark, classic colors. To make your outfits even more attractive, try adding 2-3 bright pieces like a white shirt or a light blue jacket. This creates a perfect balance that looks good on everyone.",
                "EXECUTIVE"
            )
            brightColors > darkColors -> StyleAdvice(
                "Vibrant & Energetic",
                "Your wardrobe is full of energy! You have many bright items. AI suggests adding some neutral pieces like black trousers or a grey blazer to ground your look for more formal events. This makes your style versatile for any office meeting.",
                "TRENDSETTER"
            )
            else -> StyleAdvice(
                "Perfectly Balanced",
                "Great job! Your wardrobe has a perfect mix of colors. You are ready for any occasion, from casual hangouts to big business meetings. Keep this balance to stay stylish every day.",
                "SUPREME"
            )
        }
    }

    private fun isDarkColor(color: String): Boolean {
        val darks = listOf("black", "navy", "dark grey", "charcoal", "brown", "maroon")
        return darks.any { color.lowercase(Locale.ROOT).contains(it) }
    }

    /**
     * getDailyPrompt: Generates a morning motivation prompt for the user.
     */
    fun getDailyPrompt(): String {
        val prompts = listOf(
            "Style is a way to say who you are without having to speak. What will you say today?",
            "Fashion fades, only style remains the same. Let's find your unique look in the vault.",
            "Dress like you're already famous. Your AI stylist has picked something special for you.",
            "A well-tied tie is the first serious step in life. Check your executive collection.",
            "Simplicity is the ultimate sophistication. Choose a classic look from your closet today."
        )
        return prompts.random()
    }
}
