package com.fashnix.app.ui.scan

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.fashnix.app.R

class BreathingTargetOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cornerPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary)
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var animScale = 1.0f
    private var targetRect: RectF? = null

    init {
        // IMPORTANT: Ensure this view does not intercept touch events
        isClickable = false
        isFocusable = false
        
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                animScale = 1.0f + (animation.animatedValue as Float) * 0.1f
                cornerPaint.alpha = (255 * (0.5f + (1.0f - (animation.animatedValue as Float)) * 0.5f)).toInt()
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rectWidth = w * 0.7f
        val rectHeight = h * 0.7f
        val left = (w - rectWidth) / 2
        val top = (h - rectHeight) / 2
        targetRect = RectF(left, top, left + rectWidth, top + rectHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        targetRect?.let { rect ->
            val armLength = 40f * animScale
            
            // Draw four corners
            // Top-left
            canvas.drawLine(rect.left, rect.top, rect.left + armLength, rect.top, cornerPaint)
            canvas.drawLine(rect.left, rect.top, rect.left, rect.top + armLength, cornerPaint)
            
            // Top-right
            canvas.drawLine(rect.right, rect.top, rect.right - armLength, rect.top, cornerPaint)
            canvas.drawLine(rect.right, rect.top, rect.right, rect.top + armLength, cornerPaint)
            
            // Bottom-left
            canvas.drawLine(rect.left, rect.bottom, rect.left + armLength, rect.bottom, cornerPaint)
            canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - armLength, cornerPaint)
            
            // Bottom-right
            canvas.drawLine(rect.right, rect.bottom, rect.right - armLength, rect.bottom, cornerPaint)
            canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - armLength, cornerPaint)
        }
    }
}
