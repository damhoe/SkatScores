package com.damhoe.skatscores.game_statistics

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.TypedValue

class PlotPainter {
    private lateinit var mCanvas: Canvas
    private lateinit var mTransform: Transform
    private lateinit var mStyle: PlotStyle

    private val gridPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    private val tickLabelPaint = Paint().apply {
        typeface = Typeface.DEFAULT
    }

    private val dpTickLabelDistance: Float = 5f

    fun initialize(canvas: Canvas, transform: Transform, style: PlotStyle) {
        mCanvas = canvas
        mTransform = transform
        mStyle = style

        tickLabelPaint.apply {
            textSize = dpToPx(mStyle.tickLabelSize).toFloat()
        }
    }

    fun drawGrid(
        gridDistanceX: Float = 1f,
        gridDistanceY: Float = 1f
    ) {
        val left = mTransform.viewportOffsetX
        val bottom = mTransform.viewportOffsetY
        val right = left + mTransform.viewportWidth
        val top = bottom + mTransform.viewportHeight

        val pxLeft = mTransform.toPixelX(left)
        val pxBottom = mTransform.toPixelY(bottom)
        val pxRight = mTransform.toPixelX(right)
        val pxTop = mTransform.toPixelY(top)

        var tickLabelWidth: Float // change with label text
        val tickLabelHeight = tickLabelPaint.let {
            val bounds = Rect()
            val text = "-1234567890."
            it.getTextBounds(text, 0, text.length, bounds)
            bounds.height()
        }

        val path = Path()
        // Draw borders
        path.moveTo(pxLeft, pxBottom)
        path.lineTo(pxLeft, pxTop)
        path.lineTo(pxRight, pxTop)
        path.lineTo(pxRight, pxBottom)
        path.close()
        // Draw horizontal lines
        var y = nextHigherNumberWithDivisor(bottom, gridDistanceY)
        while (y < top) {
            val pxY = mTransform.toPixelY(y)
            path.moveTo(pxLeft, pxY)
            path.lineTo(pxRight, pxY)

            // Tick label
            val tickLabel = String.format("%.0f", y)
            tickLabelWidth = tickLabelPaint.measureText(tickLabel)
            val tickLeft: Float = pxLeft - dpToPx(dpTickLabelDistance) - tickLabelWidth
            val tickBottom: Float = pxY + tickLabelHeight / 2f
            mCanvas.drawText(tickLabel, tickLeft, tickBottom, tickLabelPaint)

            y += gridDistanceY
        }
        // Draw vertical lines
        var x = nextHigherNumberWithDivisor(left, gridDistanceX)
        while (x < right) {
            val pxX = mTransform.toPixelX(x)
            path.moveTo(pxX, pxBottom)
            path.lineTo(pxX, pxTop)

            // Tick label
            val tickLabel = String.format("%.0f", x)
            tickLabelWidth = tickLabelPaint.measureText(tickLabel)
            val tickLeft: Float = pxX - tickLabelWidth / 2f
            val tickBottom: Float = pxBottom + tickLabelHeight + dpToPx(dpTickLabelDistance)
            mCanvas.drawText(tickLabel, tickLeft, tickBottom, tickLabelPaint)

            x += gridDistanceX
        }

        gridPaint.apply {
            strokeWidth = mStyle.gridStrokeWidth
            color = mStyle.gridColor
        }

        mCanvas.drawPath(path, gridPaint)
    }

    fun drawLineGraph(data: PlotData, paint: Paint) {
        val pixelData = data.toPixels(mTransform)

        drawMarkers(pixelData, paint)
        drawLinePath(pixelData, paint)
        //canvas.drawLine(px0, py0, px1, py1, )
    }

    private fun drawMarkers(data: PlotData, paint: Paint) {
        paint.apply {
            color = data.color
            style = Paint.Style.FILL
        }

        for (point: PointF in data.points.filter { mTransform.isVisible(it) }) {
            mCanvas.drawCircle(point.x, point.y, mStyle.markerSize, paint)
        }
    }

    private fun drawLinePath(data: PlotData, paint: Paint) {
        // Create the path
        val path: Path = Path()
        val points = data.points.filter { mTransform.isVisible(it) }
        val p0 = points[0]
        path.moveTo(p0.x, p0.y)
        for (k in 1..<points.size) {
            val p = points[k]
            path.lineTo(p.x, p.y)
        }

        paint.apply {
            strokeWidth = this@PlotPainter.mStyle.lineWidth
            color = data.color
            style = Paint.Style.STROKE
        }

        // Style the path
        // Draw path
        mCanvas.drawPath(path, paint)
    }

    private fun calculateVisiblePoints(points: List<PointF>): List<PointF> {
        val newPoints: MutableList<PointF> = mutableListOf()

        for (i in 0..<points.size - 1) {
            if (mTransform.isVisible(points[i])) {
                newPoints.add(points[i])

                if (!mTransform.isVisible(points[i+1])) {

                }
            } else {
                if (mTransform.isVisible(points[i+1])) {

                }
            }
        }

        // Add last element
        if (mTransform.isVisible(points.last())) {
            newPoints.add(points.last())
        }

        return newPoints
    }

    /**
     * Assumes that p0 is outside the rectangle and p1 is inside
     */
    fun calculateIntersection(p0: PointF, p1: PointF, rect: RectF): PointF {
        // TODO
        return PointF()
    }

    fun nextHigherNumberWithDivisor(x: Float, dx: Float): Float {
        if (dx == 0f) {
            throw IllegalArgumentException("The divisor 'dx' cannot be zero.")
        }

        val quotient = (x / dx).toInt() + 1
        return quotient * dx
    }

    /*
    * Calculate the bottom left coordinates for a canvas rectangle
    * where the y value increases from top to bottom
    */
    private fun calculateBottomLeft(center: PointF, width: Float, height: Float): PointF =
        PointF(center.x - 0.5f * width, center.y + 0.5f * height)

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }
}