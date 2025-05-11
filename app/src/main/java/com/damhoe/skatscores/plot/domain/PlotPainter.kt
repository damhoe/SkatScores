package com.damhoe.skatscores.plot.domain

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.TypedValue
import com.damhoe.skatscores.plot.presentation.GraphicUtils
import com.damhoe.skatscores.plot.styles.PlotStyle
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow

class PlotPainter
{
    private lateinit var mCanvas: Canvas
    private lateinit var mTransform: Transform
    private lateinit var mStyle: PlotStyle

    private val gridPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    private val tickLabelPaint = Paint().apply {
        typeface = Typeface.DEFAULT
    }

    private val legendPaint = Paint().apply {
        typeface = Typeface.DEFAULT
    }

    private val legendMarkerPaint = Paint()
    private val dpTickLabelDistance: Float = 15f

    fun initialize(
        canvas: Canvas,
        transform: Transform,
        style: PlotStyle,
    )
    {
        mCanvas = canvas
        mTransform = transform
        mStyle = style

        tickLabelPaint.apply { textSize = dpToPx(mStyle.tickLabelSize).toFloat() }
        legendPaint.apply { textSize = dpToPx(mStyle.labelSize).toFloat() }
    }

    fun drawGrid(
        gridDistanceX: Float = 1f,
        gridDistanceY: Float = 1f,
    )
    {
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
            it.getTextBounds(
                text,
                0,
                text.length,
                bounds
            )
            bounds.height()
        }

        gridPaint.apply {
            strokeWidth = mStyle.gridStrokeWidth
            color = mStyle.gridColor
        }

        val path = Path()
        // Draw borders
        path.moveTo(
            pxLeft,
            pxBottom
        )
        path.lineTo(
            pxLeft,
            pxTop
        )
        path.lineTo(
            pxRight,
            pxTop
        )
        path.lineTo(
            pxRight,
            pxBottom
        )
        path.moveTo(
            pxLeft,
            pxBottom
        )
        path.lineTo(
            pxRight,
            pxBottom
        )
        path.close()

        // Draw horizontal lines
        // Draw y tick labels
        val ratio = mTransform.viewportHeight / mTransform.height
        val (exponent, realTicks) = calculateTickPositions(
            bottom,
            top,
            ratio
        )
        val ticks = realTicks.map {
            mTransform.toPixelY(it.toFloat())
        }

        tickLabelPaint.apply {
            color = mStyle.tickLabelColor
        }

        for ((realY, y) in realTicks.zip(ticks))
        {
            mCanvas.drawLine(
                pxLeft,
                y,
                pxRight,
                y,
                gridPaint
            )

            // Add label
            val digits =
                (-exponent + 1).takeIf { it > 0 }
                    ?: 0
            val tickLabel = String.format(
                "%.${digits}f",
                realY
            )
            tickLabelWidth = tickLabelPaint.measureText(tickLabel)
            val tickLeft: Float = pxLeft - dpToPx(dpTickLabelDistance) - tickLabelWidth
            val tickBottom: Float = y + tickLabelHeight / 2f
            mCanvas.drawText(
                tickLabel,
                tickLeft,
                tickBottom,
                tickLabelPaint
            )
        }

        // Draw x tick labels
        var x = nextHigherNumberWithDivisor(
            left,
            gridDistanceX
        )
        while (x < right)
        {
            val pxX = mTransform.toPixelX(x)


            // Tick label
            val tickLabel = String.format(
                java.util.Locale.ENGLISH,
                "%.0f",
                x
            )
            tickLabelWidth = tickLabelPaint.measureText(tickLabel)
            val tickLeft: Float = pxX - tickLabelWidth / 2f
            val tickBottom: Float = pxBottom + tickLabelHeight + dpToPx(dpTickLabelDistance)
            mCanvas.drawText(
                tickLabel,
                tickLeft,
                tickBottom,
                tickLabelPaint
            )

            x += gridDistanceX
        }

        mCanvas.drawPath(
            path,
            gridPaint
        )
    }

    private val minTickDistance: Float =
        GraphicUtils.dpToPx(50f)
            .toFloat()
    private val normalizedMinTickIncrement = 0.5f // Normalized to (0.1, 1.0] interval

    /**
     * Estimate the tick positions based on the minimum tick distance
     * and the normalized tick increment. The increment is normalized to the
     * interval (0.1, 1.0]
     */
    private fun calculateTickPositions(
        min: Float,
        max: Float,
        ratio: Float,
    ): Pair<Int, List<Double>>
    {
        val realMinDist = minTickDistance * ratio
        val exponent = ceil(log10(realMinDist)).toInt()
        val minTickIncrement = normalizedMinTickIncrement * 10.0.pow(exponent)
        val increment = roundUpToNearest(
            realMinDist.toDouble(),
            minTickIncrement
        )
        val firstTick = roundUpToNearest(
            min.toDouble(),
            increment
        )
        val ticks = generateSequence(firstTick) { x ->
            (x + increment).takeIf { it <= max }
        }
        return Pair(
            exponent,
            ticks.toList()
        )
    }

    private fun roundUpToNearest(
        value: Double,
        increment: Double,
    ): Double = if (increment == 0.0)
    {
        value
    } else
    {
        ceil(value / increment) * increment
    }


    fun drawLineGraph(
        data: LineGraphData,
        paint: Paint,
    )
    {
        val pixelData = data.toPixels(
            mTransform
        )

        drawLinePath(
            pixelData,
            paint
        )

        drawMarkers(
            pixelData,
            paint
        )
    }

    private fun drawMarkers(
        data: LineGraphData,
        paint: Paint,
    )
    {
        paint.apply {
            color = gridPaint.color
            style = Paint.Style.FILL
        }

        for (point: PointF in data.points.filter { mTransform.isVisible(it) })
        {
            mCanvas.drawCircle(
                point.x,
                point.y,
                dpToPx(mStyle.markerSize).toFloat(),
                paint
            )
        }

        paint.apply {
            color = data.color
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(2f).toFloat()
        }

        for (point: PointF in data.points.filter { mTransform.isVisible(it) })
        {
            mCanvas.drawCircle(
                point.x,
                point.y,
                dpToPx(mStyle.markerSize).toFloat(),
                paint
            )
        }
    }

    private fun drawLinePath(
        data: LineGraphData,
        paint: Paint,
    )
    {
        // Create the path
        val path: Path = Path()
        val points = data.points

        if (points.isEmpty())
        {
            return
        }

        val p0 = points[0]
        path.moveTo(
            p0.x,
            p0.y
        )
        for (k in 1..<points.size)
        {
            addVisibleLineSegment(
                path,
                points[k - 1],
                points[k]
            )
        }

        paint.apply {
            strokeWidth = mStyle.lineWidth
            color = data.color
            style = Paint.Style.STROKE
        }

        // Style the path
        // Draw path
        mCanvas.drawPath(
            path,
            paint
        )
    }

    private fun addVisibleLineSegment(
        path: Path,
        point1: PointF,
        point2: PointF,
    )
    {
        // Create the path
        val isPoint1Visible = mTransform.isVisible(
            point1
        )
        val isPoint2Visible = mTransform.isVisible(
            point2
        )

        if (!isPoint1Visible and !isPoint2Visible)
        {
            return;
        }

        if (isPoint1Visible and !isPoint2Visible)
        {
            val xRight = mTransform.width.toFloat() + mTransform.insets.left
            val slope = (point2.y - point1.y) / (point2.x - point1.x)
            val yIntercept = point1.y - slope * point1.x
            val yAtXRight = slope * xRight + yIntercept

            path.moveTo(
                point1.x,
                point1.y
            )

            path.lineTo(
                xRight,
                yAtXRight,
            )
        }

        if (!isPoint1Visible and isPoint2Visible)
        {
            val xLeft = mTransform.insets.left
            val slope = (point2.y - point1.y) / (point2.x - point1.x)
            val yIntercept = point1.y - slope * point1.x
            val yAtXLeft = slope * xLeft + yIntercept

            path.moveTo(
                xLeft,
                yAtXLeft
            )

            path.lineTo(
                point2.x,
                point2.y,
            )
        }

        if (isPoint1Visible and isPoint2Visible)
        {
            path.moveTo(
                point1.x,
                point1.y
            )

            path.lineTo(
                point2.x,
                point2.y
            )
        }
    }

    fun drawLabels(
        data: List<LineGraphData>,
    )
    {
        // For each line graph add the marker and behind it
        // the label text
        val left = mTransform.insets.left.toFloat()
        val bottom = mTransform.height + mTransform.insets.bottom - 10f

        val markerSize = dpToPx(mStyle.labelMarkerSize)
        val cornerRadius = dpToPx(4f).toFloat()

        val gap = dpToPx(8f)
        var x = left

        legendPaint.apply { color = mStyle.legendColor }

        for (line in data)
        {

            legendMarkerPaint.apply { color = line.color }

            mCanvas.drawRoundRect(
                x,
                bottom - markerSize,
                x + markerSize,
                bottom,
                cornerRadius,
                cornerRadius,
                legendMarkerPaint
            )

            x += markerSize + gap
            mCanvas.drawText(
                line.label,
                x,
                bottom,
                legendPaint
            )

            x += legendPaint.measureText(line.label) + gap
        }
    }

    fun nextHigherNumberWithDivisor(
        x: Float,
        dx: Float,
    ): Float
    {
        if (dx == 0f)
        {
            throw IllegalArgumentException("The divisor 'dx' cannot be zero.")
        }

        val quotient = (x / dx).toInt() + 1
        return quotient * dx
    }

    private fun dpToPx(
        dp: Float,
    ): Int
    {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        )
            .toInt()
    }
}