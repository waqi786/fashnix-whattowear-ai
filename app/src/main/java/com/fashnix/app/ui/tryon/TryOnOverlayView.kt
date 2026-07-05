package com.fashnix.app.ui.tryon

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class TryOnOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var garmentBitmap: Bitmap? = null
    var leftShoulder: PointF? = null
    var rightShoulder: PointF? = null
    var leftHip: PointF? = null
    var rightHip: PointF? = null

    private val paint = Paint().apply {
        alpha = 190
        isFilterBitmap = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = garmentBitmap ?: return
        val ls = leftShoulder ?: return
        val rs = rightShoulder ?: return
        val lh = leftHip ?: return
        val rh = rightHip ?: return

        val shoulderWidth = Math.abs(rs.x - ls.x) * 1.1f
        val avgShoulderY = (ls.y + rs.y) / 2f
        val avgHipY = (lh.y + rh.y) / 2f
        val torsoHeight = avgHipY - avgShoulderY

        val topX = ls.x - shoulderWidth * 0.05f
        val topY = avgShoulderY - torsoHeight * 0.05f

        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
        val dstRect = RectF(topX, topY, topX + shoulderWidth, topY + torsoHeight * 1.1f)
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
    }
}