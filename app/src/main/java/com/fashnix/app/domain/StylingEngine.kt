package com.fashnix.app.domain

object StylingEngine {

    fun generateTips(category: String, occasion: String, colour: String): List<String> {
        val tips = mutableListOf<String>()

        // Category-based tips
        when (category.lowercase()) {
            "apparel" -> {
                tips.add("Layer with a jacket or cardigan for versatility.")
                if (occasion.lowercase() == "formal") {
                    tips.add("Tuck in your shirt for a polished look.")
                }
            }
            "accessories" -> {
                tips.add("Less is more — one statement piece at a time.")
            }
            "footwear" -> {
                tips.add("Match your belt colour with your shoes.")
                if (occasion.lowercase() == "casual") {
                    tips.add("Sneakers can be dressed down with rolled cuffs.")
                }
            }
            "personal care" -> {
                tips.add("Keep grooming simple and fresh.")
            }
        }

        // Occasion-based tips
        when (occasion.lowercase()) {
            "formal" -> {
                tips.add("Stick to neutral colours for a classic formal look.")
                tips.add("Ensure your outfit is wrinkle-free.")
            }
            "casual" -> {
                tips.add("Experiment with colours and patterns.")
                tips.add("Comfort is key — choose breathable fabrics.")
            }
            "party" -> {
                tips.add("Add a pop of sparkle or bold accessory.")
                tips.add("Darker shades often work well for evening events.")
            }
        }

        // Colour-based complementary advice (simplified)
        val complementary = when (colour.lowercase()) {
            "blue" -> "orange or gold accents"
            "red" -> "black or neutral accessories"
            "black" -> "almost any colour — try white or silver"
            "white" -> "any colour — earth tones create contrast"
            "other" -> "neutral accessories for balance"
            else -> "coordinated tones"
        }
        tips.add("Complement with $complementary.")

        // Return up to 3 tips
        return tips.take(3)
    }
}