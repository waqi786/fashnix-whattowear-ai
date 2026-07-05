package com.fashnix.app.util

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.fashnix.app.R

/**
 * Adds a soft press effect for image-led cards and action rows.
 */
fun View.addExpertHoverEffect() {
    val originalScale = 1.0f
    val hoverScale = 0.985f
    val touchGlow = ContextCompat.getColor(context, R.color.primary)
    val standardBorder = ContextCompat.getColor(context, R.color.border_color)
    
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                v.animate()
                    .scaleX(hoverScale)
                    .scaleY(hoverScale)
                    .setDuration(90)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
                
                if (v is com.google.android.material.card.MaterialCardView) {
                    v.setStrokeColor(ColorStateList.valueOf(touchGlow))
                    v.strokeWidth = 2
                    v.cardElevation = 12f
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(originalScale)
                    .scaleY(originalScale)
                    .setDuration(180)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
                
                if (v is com.google.android.material.card.MaterialCardView) {
                    v.setStrokeColor(ColorStateList.valueOf(standardBorder))
                    v.strokeWidth = 1
                    v.cardElevation = 2f
                }

                if (event.action == MotionEvent.ACTION_UP) {
                    v.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                }
            }
        }
        false
    }
}

fun View.animateEntrance(index: Int = 0) {
    this.alpha = 0f
    this.translationY = 28f
    this.animate()
        .alpha(1f)
        .translationY(0f)
        .setDuration(520)
        .setStartDelay(index * 50L)
        .setInterpolator(DecelerateInterpolator(1.2f))
        .start()
}
