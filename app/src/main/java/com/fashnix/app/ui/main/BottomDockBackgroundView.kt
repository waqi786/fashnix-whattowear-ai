package com.fashnix.app.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class BottomDockBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFDF7F0")
        setShadowLayer(dp(18f), 0f, dp(8f), 0x26000000)
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = Color.parseColor("#33FF5A00")
    }
    private val topGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = dp(1.2f)
        color = Color.parseColor("#40FF8A1F")
    }
    private val dockPath = Path()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, dockPaint)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val widthF = width.toFloat()
        val heightF = height.toFloat()
        val barTop = dp(28f)
        val radius = dp(24f)
        val centerX = widthF / 2f
        val cutoutRadius = dp(42f)
        val cutoutControl = dp(18f)

        dockPath.reset()
        dockPath.moveTo(radius, barTop)
        dockPath.lineTo(centerX - cutoutRadius - cutoutControl, barTop)
        dockPath.cubicTo(
            centerX - cutoutRadius,
            barTop,
            centerX - cutoutRadius,
            barTop + dp(26f),
            centerX,
            barTop + dp(26f)
        )
        dockPath.cubicTo(
            centerX + cutoutRadius,
            barTop + dp(26f),
            centerX + cutoutRadius,
            barTop,
            centerX + cutoutRadius + cutoutControl,
            barTop
        )
        dockPath.lineTo(widthF - radius, barTop)
        dockPath.quadTo(widthF, barTop, widthF, barTop + radius)
        dockPath.lineTo(widthF, heightF - radius)
        dockPath.quadTo(widthF, heightF, widthF - radius, heightF)
        dockPath.lineTo(radius, heightF)
        dockPath.quadTo(0f, heightF, 0f, heightF - radius)
        dockPath.lineTo(0f, barTop + radius)
        dockPath.quadTo(0f, barTop, radius, barTop)
        dockPath.close()

        canvas.drawPath(dockPath, dockPaint)
        canvas.drawPath(dockPath, strokePaint)
        canvas.drawLine(dp(24f), barTop + dp(1f), widthF - dp(24f), barTop + dp(1f), topGlowPaint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
