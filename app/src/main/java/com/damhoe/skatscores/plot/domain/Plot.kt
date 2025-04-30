package com.damhoe.skatscores.plot.domain

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.damhoe.skatscores.plot.styles.PlotStyle
import com.damhoe.skatscores.plot.presentation.GraphicUtils.dpToPx

class Plot(
    val bounds: RectF,
)
{
    val style: PlotStyle = PlotStyle(
        markerSize = dpToPx(3f).toFloat(),
        lineWidth = dpToPx(3f).toFloat(),
        gridStrokeWidth = dpToPx(2f).toFloat(),
        gridColor = Color.BLACK.hashCode(),
        tickLabelSize = 16f,
        labelSize = 16f,
        labelMarkerSize = 16f,
        tickLabelColor = Color.BLACK.hashCode(),
        legendColor = Color.BLACK.hashCode()
    )

    val paint: Paint = Paint()

    var gridDistanceX: Float = 1f
    var gridDistanceY: Float = 1f

    var lineGraphs: MutableList<LineGraphData> = mutableListOf()

    // Allocate here to optimize drawing
    private val painter = PlotPainter()

    fun draw(
        canvas: Canvas,
        transform: Transform,
    )
    {
        painter.initialize(
            canvas = canvas,
            transform = transform,
            style = style
        )

        gridDistanceY = transform.viewportHeight / transform.height * 100

        painter.drawGrid(
            gridDistanceX = gridDistanceX,
            gridDistanceY = gridDistanceY
        )

        for (line: LineGraphData in lineGraphs)
        {
            painter.drawLineGraph(
                line,
                paint
            )
        }

        painter.drawLabels(
            lineGraphs
        )
    }
}