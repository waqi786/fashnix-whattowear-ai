package com.fashnix.app.util

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * A world-class Page Transformer that adds depth, scale, and parallax.
 * Used by top-tier designers to make carousels feel "alive".
 */
class ExpertPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.apply {
            val absPos = abs(position)
            
            // Scaling Effect
            val scale = if (absPos > 1) 0.85f else 1 - (absPos * 0.15f)
            scaleX = scale
            scaleY = scale
            
            // Alpha/Fade Effect
            alpha = if (absPos > 1) 0.5f else 1 - (absPos * 0.5f)
            
            // 3D Rotation / Depth
            rotationY = position * -20f
            
            // Parallax movement for internal elements (if any)
            translationX = position * -width / 4
        }
    }
}