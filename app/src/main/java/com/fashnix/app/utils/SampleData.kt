package com.fashnix.app.utils

import com.fashnix.app.data.model.ClothingItem

object SampleData {
    fun getSampleItems(userId: String): List<ClothingItem> {
        val blazerImage = "https://images.unsplash.com/photo-1506629905607-d405b7a30db9?auto=format&fit=crop&w=1200&q=90"
        val suitImage = "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=1200&q=90"
        val shirtImage = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=1200&q=90"
        val jacketImage = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=1200&q=90"
        val jeansImage = "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?auto=format&fit=crop&w=1200&q=90"
        val sneakersImage = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=90"
        val heelsImage = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=1200&q=90"
        val dressImage = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=1200&q=90"
        val coatImage = "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?auto=format&fit=crop&w=1200&q=90"
        val watchImage = "https://images.unsplash.com/photo-1523170335258-f5ed11844a49?auto=format&fit=crop&w=1200&q=90"
        val bagImage = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=1200&q=90"
        val glassesImage = "https://images.unsplash.com/photo-1572635196237-14b3f281503f?auto=format&fit=crop&w=1200&q=90"
        val scarfImage = "https://images.unsplash.com/photo-1601762603339-fd61e28b698a?auto=format&fit=crop&w=1200&q=90"
        val kurtaImage = "https://images.unsplash.com/photo-1597983073493-88cd35cf93b0?auto=format&fit=crop&w=1200&q=90"
        val sareeImage = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=1200&q=90"
        val poloImage = "https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?auto=format&fit=crop&w=1200&q=90"
        val hoodieImage = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=1200&q=90"
        val loaferImage = "https://images.unsplash.com/photo-1614252369475-531eba835eb1?auto=format&fit=crop&w=1200&q=90"
        return listOf(
            ClothingItem(
                id = "seed_m1", userId = userId, name = "Black Slim Fit Suit",
                category = "Apparel", color = "Black", occasion = "Formal", gender = "Men",
                brand = "Premium Stitch", price = 850.0, wearCount = 4, laundryStatus = "Clean",
                imageUrl = suitImage
            ),
            ClothingItem(
                id = "seed_m2", userId = userId, name = "White Oxford Shirt",
                category = "Apparel", color = "White", occasion = "Work", gender = "Men",
                brand = "Daily Wear", price = 75.0, wearCount = 2, laundryStatus = "Clean",
                imageUrl = shirtImage
            ),
            ClothingItem(
                id = "seed_m3", userId = userId, name = "Blue Denim Jacket",
                category = "Apparel", color = "Blue", occasion = "Casual", gender = "Unisex",
                brand = "Urban Style", price = 120.0, wearCount = 8, laundryStatus = "Clean",
                imageUrl = jacketImage
            ),
            ClothingItem(
                id = "seed_m4", userId = userId, name = "Navy Smart Blazer",
                category = "Apparel", color = "Navy", occasion = "Office", gender = "Men",
                brand = "SmartFit", price = 195.0, wearCount = 5, laundryStatus = "Clean",
                imageUrl = blazerImage
            ),
            ClothingItem(
                id = "seed_w1", userId = userId, name = "Red Evening Gown",
                category = "Apparel", color = "Red", occasion = "Party", gender = "Women",
                brand = "Glow Fashion", price = 450.0, wearCount = 2, laundryStatus = "Clean",
                imageUrl = dressImage
            ),
            ClothingItem(
                id = "seed_w2", userId = userId, name = "Beige Trench Coat",
                category = "Apparel", color = "Beige", occasion = "Winter", gender = "Women",
                brand = "Urban Chic", price = 250.0, wearCount = 5, laundryStatus = "Clean",
                imageUrl = coatImage
            ),
            ClothingItem(
                id = "seed_w3", userId = userId, name = "Floral Summer Dress",
                category = "Apparel", color = "Multi", occasion = "Casual", gender = "Women",
                brand = "SunKissed", price = 85.0, wearCount = 1, laundryStatus = "Clean",
                imageUrl = dressImage
            ),
            ClothingItem(
                id = "seed_w4", userId = userId, name = "Black Satin Heels",
                category = "Footwear", color = "Black", occasion = "Party", gender = "Women",
                brand = "Elegant Steps", price = 165.0, wearCount = 3, laundryStatus = "Clean",
                imageUrl = heelsImage
            ),
            ClothingItem(
                id = "seed_a1", userId = userId, name = "Luxury Gold Watch",
                category = "Accessories", color = "Gold", occasion = "Formal", gender = "Unisex",
                brand = "TimeMaster", price = 520.0, wearCount = 12, laundryStatus = "Clean",
                imageUrl = watchImage
            ),
            ClothingItem(
                id = "seed_a2", userId = userId, name = "Black Leather Handbag",
                category = "Accessories", color = "Black", occasion = "Work", gender = "Women",
                brand = "LuxCarry", price = 320.0, wearCount = 9, laundryStatus = "Clean",
                imageUrl = bagImage
            ),
            ClothingItem(
                id = "seed_a3", userId = userId, name = "Aviator Sunglasses",
                category = "Accessories", color = "Black", occasion = "Casual", gender = "Unisex",
                brand = "SunGuard", price = 180.0, wearCount = 15, laundryStatus = "Clean",
                imageUrl = glassesImage
            ),
            ClothingItem(
                id = "seed_a4", userId = userId, name = "Silk Floral Scarf",
                category = "Accessories", color = "Multi", occasion = "Casual", gender = "Women",
                brand = "SoftTouch", price = 65.0, wearCount = 4, laundryStatus = "Clean",
                imageUrl = scarfImage
            ),
            ClothingItem(
                id = "seed_e1", userId = userId, name = "White Traditional Kurta",
                category = "Apparel", color = "White", occasion = "Event", gender = "Men",
                brand = "Heritage", price = 120.0, wearCount = 2, laundryStatus = "Clean",
                imageUrl = kurtaImage
            ),
            ClothingItem(
                id = "seed_e2", userId = userId, name = "Green Silk Saree",
                category = "Apparel", color = "Green", occasion = "Wedding", gender = "Women",
                brand = "Royal Threads", price = 850.0, wearCount = 1, laundryStatus = "Clean",
                imageUrl = sareeImage
            ),
            ClothingItem(
                id = "seed_s1", userId = userId, name = "Red Sport Sneakers",
                category = "Footwear", color = "Red", occasion = "Sports", gender = "Unisex",
                brand = "FastRun", price = 110.0, wearCount = 6, laundryStatus = "Clean",
                imageUrl = sneakersImage
            ),
            ClothingItem(
                id = "seed_s2", userId = userId, name = "Classic White Sneakers",
                category = "Footwear", color = "White", occasion = "Casual", gender = "Unisex",
                brand = "CleanSteps", price = 95.0, wearCount = 7, laundryStatus = "Clean",
                imageUrl = sneakersImage
            ),
            ClothingItem(
                id = "seed_s3", userId = userId, name = "Tan Leather Loafers",
                category = "Footwear", color = "Brown", occasion = "Office", gender = "Men",
                brand = "Boardroom", price = 140.0, wearCount = 4, laundryStatus = "Clean",
                imageUrl = loaferImage
            ),
            ClothingItem(
                id = "seed_s4", userId = userId, name = "Blue Denim Jeans",
                category = "Apparel", color = "Blue", occasion = "Casual", gender = "Unisex",
                brand = "Everyday Denim", price = 60.0, wearCount = 5, laundryStatus = "Clean",
                imageUrl = jeansImage
            ),
            ClothingItem(
                id = "seed_c1", userId = userId, name = "Orange Knit Polo",
                category = "Apparel", color = "Orange", occasion = "Casual", gender = "Men",
                brand = "Fashnix Core", price = 58.0, wearCount = 1, laundryStatus = "Clean",
                imageUrl = poloImage
            ),
            ClothingItem(
                id = "seed_c2", userId = userId, name = "Black Travel Hoodie",
                category = "Apparel", color = "Black", occasion = "Travel", gender = "Unisex",
                brand = "Airport Edit", price = 90.0, wearCount = 2, laundryStatus = "Clean",
                imageUrl = hoodieImage
            )
        )
    }
}
