package com.fashnix.app.domain

object DressCodeRules {
    val rules: Map<String, String> = mapOf(
        "Job Interview" to "Wear a well-fitted suit or formal trousers and a button-down shirt. Stick to neutral colours like navy, grey, or black. Polished leather shoes and minimal accessories complete the look.",
        "Cocktail Party" to "Semi-formal attire — a dress or suit in dark or rich colours. For women, a knee-length cocktail dress; for men, a dark suit with a tie optional. Heels or dress shoes recommended.",
        "Beach Wedding" to "Light, breathable fabrics in pastel or bright colours. Linen suits or summer dresses. Flat sandals or espadrilles. Avoid black and heavy fabrics.",
        "Festive Ethnic (Eid/Diwali)" to "Embrace traditional attire — sherwani, kurta-pajama, saree, or lehenga. Rich fabrics, embroidery, and vibrant colours. Gold jewellery adds festive flair.",
        "Funeral / Memorial" to "Sombre, respectful attire in black, dark grey, or navy. Simple suits or modest dresses. Avoid bright colours and flashy accessories.",
        "Black Tie Gala" to "Formal evening wear: tuxedo with bow tie for men, floor-length gown for women. Elegant jewellery and clutch. Black patent leather shoes.",
        "First Date" to "Smart casual — clean, well-fitted outfit that shows your style without trying too hard. Dark jeans with a nice top or a casual dress. Comfortable yet stylish shoes.",
        "Casual Friday" to "Relaxed but not sloppy. Dark jeans or chinos, polo shirt or casual button-down. Clean sneakers or loafers. Avoid shorts and flip-flops in many offices.",
        "Sports Event" to "Team colours and comfortable clothing. Jerseys, hoodies, caps. Sneakers are a must. Dress for the weather if outdoors.",
        "Destination Wedding" to "Depends on location: tropical calls for light, airy attire (linen, cotton); mountain settings need layers. Check the invite for theme. Always pack a stylish cover-up or shawl.",
        "Birthday Party" to "Match the vibe of the party — casual, themed, or dressy. A fun dress, stylish jumpsuit, or smart casual for men. Feel free to accessorize and add colour.",
        "Business Casual" to "A step down from formal: chinos or dress pants, button-down or polo shirt, optional blazer. Loafers or oxfords. For women, tailored trousers or skirt with a blouse or sweater."
    )

    fun getFallbackText(event: String): String {
        return rules[event] ?: "Dress appropriately for the occasion, aiming for neatness and comfort. When in doubt, smart casual rarely fails."
    }
}