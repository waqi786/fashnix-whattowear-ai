package com.fashnix.app.domain

import com.fashnix.app.data.model.AccessorySet
import com.fashnix.app.data.model.ClothingItem
import com.fashnix.app.data.model.Suggestion

object AccessoryMatcher {

    fun generateCompleteAccessorySet(
        item: ClothingItem,
        uvIndex: Float,
        userWardrobe: List<ClothingItem>
    ): AccessorySet {
        return AccessorySet(
            shoes = suggestShoes(item, userWardrobe),
            belt = suggestBelt(item, userWardrobe),
            watch = suggestWatch(item, userWardrobe),
            tie = suggestTie(item, userWardrobe),
            pocketSquare = suggestPocketSquare(item, userWardrobe),
            jewelry = suggestJewelry(item, userWardrobe),
            bag = suggestBag(item, userWardrobe),
            sunglasses = suggestSunglasses(item, uvIndex, userWardrobe)
        )
    }

    fun suggestShoes(item: ClothingItem, userWardrobe: List<ClothingItem>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val color = item.color.lowercase()
        val occasion = item.occasion.lowercase()
        val gender = item.gender.lowercase()

        when {
            (color == "navy" || color == "blue") && occasion == "formal" -> {
                suggestions.add(createSuggestion("Brown Oxford", "Brown", "Shoes",
                    "Brown leather Oxfords complement navy perfectly for a formal look", "Oxford", userWardrobe))
                suggestions.add(createSuggestion("Black Oxford", "Black", "Shoes",
                    "Classic black Oxfords are always safe with navy", "Oxford", userWardrobe))
            }
            color == "black" -> {
                suggestions.add(createSuggestion("Black Oxford", "Black", "Shoes",
                    "Classic black Oxfords are always safe", "Oxford", userWardrobe))
                suggestions.add(createSuggestion("Black Derby", "Black", "Shoes",
                    "Black Derbys for a slightly relaxed formal look", "Derby", userWardrobe))
            }
            (color == "beige" || color == "other") && occasion == "casual" -> {
                suggestions.add(createSuggestion("White Sneakers", "White", "Shoes",
                    "White sneakers give a fresh casual contrast", "Sneakers", userWardrobe))
                suggestions.add(createSuggestion("Tan Loafers", "Tan", "Shoes",
                    "Tan loafers elevate your casual look", "Loafers", userWardrobe))
            }
            color == "red" && occasion == "party" -> {
                if (gender == "women") {
                    suggestions.add(createSuggestion("Black Heels", "Black", "Shoes",
                        "Black heels elongate the leg and match any party outfit", "Heels", userWardrobe))
                } else {
                    suggestions.add(createSuggestion("Black Oxford", "Black", "Shoes",
                        "Black Oxfords keep it sharp for party", "Oxford", userWardrobe))
                }
            }
            (color == "white" || color == "light") && occasion == "casual" -> {
                suggestions.add(createSuggestion("White Sneakers", "White", "Shoes",
                    "Fresh white sneakers for a clean casual vibe", "Sneakers", userWardrobe))
                suggestions.add(createSuggestion("Canvas Slip-ons", "Grey", "Shoes",
                    "Easy canvas slip-ons for warm days", "Slip-on", userWardrobe))
            }
            else -> {
                suggestions.add(createSuggestion("Classic Leather Shoes", "Brown", "Shoes",
                    "Versatile leather shoes suitable for most occasions", "Leather", userWardrobe))
            }
        }
        return suggestions
    }

    fun suggestBelt(item: ClothingItem, userWardrobe: List<ClothingItem>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val occasion = item.occasion.lowercase()
        val beltColor = item.color.lowercase()
        val material = if (occasion == "formal") "leather" else "canvas"
        val reason = if (occasion == "formal")
            "A $beltColor leather belt ties your formal look together"
        else
            "A $beltColor $material belt adds a relaxed touch"
        suggestions.add(createSuggestion("$beltColor Belt", beltColor, "Belt", reason, "Belt", userWardrobe))
        return suggestions
    }

    fun suggestWatch(item: ClothingItem, userWardrobe: List<ClothingItem>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val occasion = item.occasion.lowercase()
        when (occasion) {
            "formal" -> {
                suggestions.add(createSuggestion("Classic leather-strap watch", "Black", "Watch",
                    "A leather strap watch is timeless and elegant", "Watch", userWardrobe))
                suggestions.add(createSuggestion("Metal bracelet dress watch", "Silver", "Watch",
                    "A metal bracelet adds a modern touch to formal wear", "Watch", userWardrobe))
            }
            "casual" -> {
                suggestions.add(createSuggestion("Sports watch", "Black", "Watch",
                    "A sports watch is perfect for everyday casual", "Watch", userWardrobe))
                suggestions.add(createSuggestion("NATO strap watch", "Blue", "Watch",
                    "A NATO strap gives a laid-back, adventurous feel", "Watch", userWardrobe))
            }
            "party" -> {
                suggestions.add(createSuggestion("Statement gold watch", "Gold", "Watch",
                    "A gold statement watch shines at parties", "Watch", userWardrobe))
                suggestions.add(createSuggestion("Rose gold chronograph", "Rose Gold", "Watch",
                    "A rose gold chronograph adds a sophisticated sparkle", "Watch", userWardrobe))
            }
            else -> {
                suggestions.add(createSuggestion("Minimalist watch", "Silver", "Watch",
                    "A sleek, minimalist watch goes with everything", "Watch", userWardrobe))
            }
        }
        return suggestions
    }

    fun suggestTie(item: ClothingItem, userWardrobe: List<ClothingItem>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        if (item.gender.lowercase() != "men" ||
            (item.occasion.lowercase() != "formal" && item.occasion.lowercase() != "party"))
            return suggestions

        val shirtColor = item.color.lowercase()
        when {
            shirtColor == "white" -> {
                suggestions.add(createSuggestion("Navy Silk Tie", "Navy", "Tie",
                    "A navy silk tie creates elegant contrast with a white shirt", "Tie", userWardrobe))
                suggestions.add(createSuggestion("Burgundy Tie", "Burgundy", "Tie",
                    "Burgundy adds a rich pop of colour", "Tie", userWardrobe))
                suggestions.add(createSuggestion("Charcoal Tie", "Charcoal", "Tie",
                    "Charcoal is understated and sophisticated", "Tie", userWardrobe))
            }
            shirtColor == "blue" -> {
                suggestions.add(createSuggestion("Gold Tie", "Gold", "Tie",
                    "A gold tie creates a striking contrast with blue", "Tie", userWardrobe))
                suggestions.add(createSuggestion("Silver Tie", "Silver", "Tie",
                    "Silver tie for a cool, coordinated look", "Tie", userWardrobe))
            }
            shirtColor == "black" -> {
                suggestions.add(createSuggestion("Grey Tie", "Grey", "Tie",
                    "A grey tie softens a black shirt with style", "Tie", userWardrobe))
            }
            else -> {
                suggestions.add(createSuggestion("Patterned Silk Tie", "Mixed", "Tie",
                    "A patterned tie adds personality", "Tie", userWardrobe))
            }
        }
        return suggestions
    }

    fun suggestPocketSquare(item: ClothingItem, userWardrobe: List<ClothingItem>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        if (item.category.lowercase() != "apparel" || item.occasion.lowercase() != "formal")
            return suggestions
        suggestions.add(createSuggestion("White Pocket Square", "White", "Pocket Square",
            "A crisp white pocket square is the classic finishing touch", "Pocket Square", userWardrobe))
        suggestions.add(createSuggestion("Matching Tone Pocket Square", item.color, "Pocket Square",
            "A pocket square that picks up your tie colour adds tonal elegance", "Pocket Square", userWardrobe))
        return suggestions
    }

    fun suggestJewelry(item: ClothingItem, userWardrobe: List<ClothingItem>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        if (item.gender.lowercase() != "women") return suggestions
        val occasion = item.occasion.lowercase()
        when (occasion) {
            "formal" -> {
                suggestions.add(createSuggestion("Pearl Stud Earrings", "White", "Jewelry",
                    "Pearl studs are timeless formal elegance", "Earrings", userWardrobe))
                suggestions.add(createSuggestion("Gold Pendant Necklace", "Gold", "Jewelry",
                    "A gold pendant adds a delicate focus point", "Necklace", userWardrobe))
            }
            "party" -> {
                suggestions.add(createSuggestion("Statement Chandelier Earrings", "Gold", "Jewelry",
                    "Chandelier earrings make a dramatic party statement", "Earrings", userWardrobe))
                suggestions.add(createSuggestion("Layered Necklace", "Silver", "Jewelry",
                    "Layered necklaces add texture and glamour", "Necklace", userWardrobe))
            }
            "casual" -> {
                suggestions.add(createSuggestion("Simple Hoop Earrings", "Gold", "Jewelry",
                    "Simple hoops are effortlessly chic", "Earrings", userWardrobe))
                suggestions.add(createSuggestion("Delicate Bracelet", "Gold", "Jewelry",
                    "A delicate bracelet adds a subtle feminine touch", "Bracelet", userWardrobe))
            }
            else -> {
                suggestions.add(createSuggestion("Stud Earrings", "Silver", "Jewelry",
                    "Simple studs complement any look", "Earrings", userWardrobe))
            }
        }
        return suggestions
    }

    fun suggestBag(item: ClothingItem, userWardrobe: List<ClothingItem>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val gender = item.gender.lowercase()
        val occasion = item.occasion.lowercase()
        if (gender == "women") {
            when (occasion) {
                "formal" -> suggestions.add(createSuggestion("Structured Leather Clutch", "Black", "Bag",
                    "A structured clutch is the ultimate formal accessory", "Clutch", userWardrobe))
                "casual" -> suggestions.add(createSuggestion("Canvas Tote", "Beige", "Bag",
                    "A roomy canvas tote for casual chic", "Tote", userWardrobe))
                "party" -> suggestions.add(createSuggestion("Sequin Evening Bag", "Gold", "Bag",
                    "A sequin bag brings the party sparkle", "Evening Bag", userWardrobe))
                else -> suggestions.add(createSuggestion("Crossbody Bag", "Black", "Bag",
                    "A compact crossbody bag for hands-free style", "Crossbody", userWardrobe))
            }
        } else {
            when (occasion) {
                "formal" -> suggestions.add(createSuggestion("Leather Briefcase", "Brown", "Bag",
                    "A leather briefcase commands respect", "Briefcase", userWardrobe))
                "casual" -> suggestions.add(createSuggestion("Canvas Backpack", "Grey", "Bag",
                    "A canvas backpack for practical casual carry", "Backpack", userWardrobe))
                else -> suggestions.add(createSuggestion("Messenger Bag", "Brown", "Bag",
                    "A messenger bag blends functionality with style", "Messenger", userWardrobe))
            }
        }
        return suggestions
    }

    fun suggestSunglasses(item: ClothingItem, uvIndex: Float, userWardrobe: List<ClothingItem>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        if (uvIndex > 5) {
            suggestions.add(createSuggestion("UV Protection Sunglasses", "Black", "Sunglasses",
                "UV index is high today — protect your eyes", "Sunglasses", userWardrobe))
        }
        val occasion = item.occasion.lowercase()
        val gender = item.gender.lowercase()
        when {
            occasion == "casual" -> {
                suggestions.add(createSuggestion("Classic Wayfarer", "Black", "Sunglasses",
                    "Wayfarers are the iconic casual sunglass", "Sunglasses", userWardrobe))
                suggestions.add(createSuggestion("Round Lens", "Brown", "Sunglasses",
                    "Round lenses add a retro-casual vibe", "Sunglasses", userWardrobe))
            }
            occasion == "formal" || occasion == "smart-casual" -> {
                suggestions.add(createSuggestion("Aviator", "Gold", "Sunglasses",
                    "Aviators are sleek and sophisticated", "Sunglasses", userWardrobe))
            }
            gender == "women" && occasion == "party" -> {
                suggestions.add(createSuggestion("Cat-Eye Sunglasses", "Black", "Sunglasses",
                    "Cat-eye frames bring old-Hollywood glamour", "Sunglasses", userWardrobe))
            }
            else -> {
                suggestions.add(createSuggestion("Polarized Sunglasses", "Black", "Sunglasses",
                    "Polarized lenses reduce glare and look great", "Sunglasses", userWardrobe))
            }
        }
        return suggestions
    }

    private fun createSuggestion(
        name: String,
        color: String,
        type: String,
        reason: String,
        searchName: String,
        userWardrobe: List<ClothingItem>
    ): Suggestion {
        val match = userWardrobe.find {
            it.name.contains(searchName, ignoreCase = true) ||
            it.category.equals(type, ignoreCase = true)
        }
        return Suggestion(
            itemName = name,
            color = color,
            type = type,
            reason = reason,
            fromWardrobe = match != null,
            wardrobeItemId = match?.id
        )
    }
}