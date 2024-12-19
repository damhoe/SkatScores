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
import com.damhoe.skatscores.plot.styles.PlotStyle
import com.damhoe.skatscores.plot.presentation.GraphicUtils
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
                bounds)
            bounds.height()
        }
        
        val path = Path()
        // Draw borders
        path.moveTo(
            pxLeft,
            pxBottom)
        path.lineTo(
            pxLeft,
            pxTop)
        path.lineTo(
            pxRight,
            pxTop)
        path.lineTo(
            pxRight,
            pxBottom)
        path.moveTo(
            pxLeft,
            pxBottom)
        path.lineTo(
            pxRight,
            pxBottom)
        path.close()
        // Draw horizontal lines
        var y = nextHigherNumberWithDivisor(
            bottom,
            gridDistanceY)
        
        // Draw x ticks
        val ratio = mTransform.viewportHeight / mTransform.height
        val (exponent, realTicks) = calculateTickPositions(
            bottom,
            top,
            ratio)
        val ticks = realTicks.map { mTransform.toPixelY(it.toFloat()) }
        
        tickLabelPaint.apply { color = mStyle.tickLabelColor }
        
        for ((realY, y) in realTicks.zip(ticks))
        {
            mCanvas.drawLine(
                pxLeft,
                y,
                pxRight,
                y,
                gridPaint)
            
            // Add label
            val digits =
                (-exponent + 1).takeIf { it > 0 }
                ?: 0
            val tickLabel = String.format(
                "%.${digits}f",
                realY)
            tickLabelWidth = tickLabelPaint.measureText(tickLabel)
            val tickLeft: Float = pxLeft - dpToPx(dpTickLabelDistance) - tickLabelWidth
            val tickBottom: Float = y + tickLabelHeight / 2f
            mCanvas.drawText(
                tickLabel,
                tickLeft,
                tickBottom,
                tickLabelPaint)
        }
        
        // Draw vertical lines
        var x = nextHigherNumberWithDivisor(
            left,
            gridDistanceX)
        while (x < right)
        {
            val pxX = mTransform.toPixelX(x)
            //path.moveTo(pxX, pxBottom)
            //path.lineTo(pxX, pxTop)
            
            // Tick label
            val tickLabel = String.format(
                "%.0f",
                x)
            tickLabelWidth = tickLabelPaint.measureText(tickLabel)
            val tickLeft: Float = pxX - tickLabelWidth / 2f
            val tickBottom: Float = pxBottom + tickLabelHeight + dpToPx(dpTickLabelDistance)
            mCanvas.drawText(
                tickLabel,
                tickLeft,
                tickBottom,
                tickLabelPaint)
            
            x += gridDistanceX
        }
        
        gridPaint.apply {
            strokeWidth = mStyle.gridStrokeWidth
            color = mStyle.gridColor
        }
        
        mCanvas.drawPath(
            path,
            gridPaint)
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
            minTickIncrement)
        val firstTick = roundUpToNearest(
            min.toDouble(),
            increment)
        val ticks = generateSequence(firstTick) { x ->
            (x + increment).takeIf { it <= max }
        }
        return Pair(
            exponent,
            ticks.toList())
    }
    
    private fun roundUpToNearest(
        value: Double,
        increment: Double,
    ): Double = if (increment == 0.0)
    {
        value
    }
    else
    {
        ceil(value / increment) * increment
    }
    
    
    fun drawLineGraph(
        data: LineGraphData,
        paint: Paint,
    )
    {
        val pixelData = data.toPixels(mTransform)
        
        drawLinePath(
            pixelData,
            paint)
        drawMarkers(
            pixelData,
            paint)
        //canvas.drawLine(px0, py0, px1, py1, )
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
                paint)
        }
        
        paint.apply {
            color = data.color
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        
        for (point: PointF in data.points.filter { mTransform.isVisible(it) })
        {
            mCanvas.drawCircle(
                point.x,
                point.y,
                dpToPx(mStyle.markerSize).toFloat(),
                paint)
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
            .filter { mTransform.isVisible(it) }
        
        if (points.isEmpty())
        {
            return
        }
        
        val p0 = points[0]
        path.moveTo(
            p0.x,
            p0.y)
        for (k in 1..<points.size)
        {
            val p = points[k]
            path.lineTo(
                p.x,
                p.y)
        }
        
        paint.apply {
            strokeWidth = dpToPx(this@PlotPainter.mStyle.lineWidth).toFloat()
            color = data.color
            style = Paint.Style.STROKE
        }
        
        // Style the path
        // Draw path
        mCanvas.drawPath(
            path,
            paint)
    }
    
    fun drawLabels(data: List<LineGraphData>)
    {
        
        // For each line graph add the marker and behind it
        // the label text
        val left = mTransform.insets.left.toFloat()
        val bottom = mTransform.height + mTransform.insets.bottom - 10f
        
        val markerSize = dpToPx(mStyle.labelMarkerSize)
        val cornerRadius = dpToPx(4f).toFloat()
        
        val gap = dpToPx(5f)
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
                legendMarkerPaint)
            
            x += markerSize + gap
            mCanvas.drawText(
                line.label,
                x,
                bottom,
                legendPaint)
            
            x += legendPaint.measureText(line.label) + gap
        }
    }
    
    private fun calculateVisiblePoints(points: List<PointF>): List<PointF>
    {
        val newPoints: MutableList<PointF> = mutableListOf()
        
        for (i in 0..<points.size - 1)
        {
            if (mTransform.isVisible(points[i]))
            {
                newPoints.add(points[i])
                
                if (!mTransform.isVisible(points[i + 1]))
                {
                
                }
            }
            else
            {
                if (mTransform.isVisible(points[i + 1]))
                {
                
                }
            }
        }
        
        // Add last element
        if (mTransform.isVisible(points.last()))
        {
            newPoints.add(points.last())
        }
        
        return newPoints
    }
    
    /**
     * Assumes that p0 is outside the rectangle and p1 is inside
     */
    fun calculateIntersection(
        p0: PointF,
        p1: PointF,
        rect: RectF,
    ): PointF
    {
        // TODO
        return PointF()
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
    
    /*
    * Calculate the bottom left coordinates for a canvas rectangle
    * where the y value increases from top to bottom
    */
    private fun calculateBottomLeft(
        center: PointF,
        width: Float,
        height: Float,
    ): PointF = PointF(
        center.x - 0.5f * width,
        center.y + 0.5f * height)
    
    private fun dpToPx(
        dp: Float,
    ): Int
    {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics)
            .toInt()
    }
}