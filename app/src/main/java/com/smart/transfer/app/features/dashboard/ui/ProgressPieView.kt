package com.smart.transfer.app.com.smart.transfer.app.features.dashboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class ProgressPieView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val colors = intArrayOf(
        0xFF4CBA65.toInt(),  // Images
        0xFFFF792D.toInt(),  //  Videos
        0xFFF6B43B.toInt(),  //   Documents
        0xFF2AABE3.toInt()  , //  for Audio
        0xFFBA3BF6.toInt() ,  //  for Available
        0xFFEBEBEB.toInt()   ,//  for System ,


    )

    private var progressValues = floatArrayOf(0f, 0f, 0f, 0f,0f) // Progress values for each file type
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    // Convert 10dp to pixels
    private val strokeWidthPx = 10f.dpToPx(context)

    init {
        paint.style = Paint.Style.STROKE // Set paint to stroke mode
        paint.strokeWidth = strokeWidthPx // Set the stroke width to 10dp
        paint.strokeCap = Paint.Cap.ROUND // Optional: Rounded edges for the arcs
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val diameter = width.coerceAtMost(height)

        // Adjust the rectangle to account for the stroke width
        rectF.set(
            strokeWidthPx / 2,
            strokeWidthPx / 2,
            diameter - strokeWidthPx / 2,
            diameter - strokeWidthPx / 2
        )

        var startAngle = -90f // Start from the top

        for (i in progressValues.indices) {
            paint.color = colors[i]
            val sweepAngle = 360 * (progressValues[i] / 100)
            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
            startAngle += sweepAngle
        }
    }

    // Extension function to convert dp to pixels
    private fun Float.dpToPx(context: Context): Float {
        return this * context.resources.displayMetrics.density
    }

    /**
     * Set storage sizes for each file type and calculate progress values.
     *
     * @param totalSize Total storage size in GB.
     * @param imageSize Size of images in GB.
     * @param videoSize Size of videos in GB.
     * @param docSize Size of documents in GB.
     * @param audioSize Size of audio files in GB.
     */
    fun setStorageSizes(totalSize: Float, imageSize: Float, videoSize: Float, docSize: Float, audioSize: Float, availableSize: Float,systemSize:Float) {
        if (totalSize <= 0) {
            throw IllegalArgumentException("Total size must be greater than 0")
        }

        // Calculate percentages for each file type
        progressValues = floatArrayOf(
            (imageSize / totalSize) * 100,
            (videoSize / totalSize) * 100,
            (docSize / totalSize) * 100,
            (audioSize / totalSize) * 100,
            (systemSize / totalSize) * 100  ,     // System storage
            (availableSize / totalSize) * 100 ,

        )

        // Log the calculated percentages
        println("Progress Values: ${progressValues.joinToString(", ")}")

        // Ensure the sum is 100 (handle floating-point inaccuracies)
        val sum = progressValues.sum()
        if (sum != 100f) {
            progressValues[4] += 100f - sum // Adjust the last segment (available storage)
        }

        invalidate() // Redraw the view
    }
}
