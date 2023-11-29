package com.damhoe.skatscores.game_statistics

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.TypedValue

class Plot(val bounds: RectF) {

    val style: PlotStyle = PlotStyle().apply {
        markerSize = dpToPx(4f)
        lineWidth = dpToPx(2f)
        gridStrokeWidth = dpToPx(1f)
        gridColor = Color.GRAY.hashCode()
        tickLabelSize = 11f
    }

    val paint: Paint = Paint()

    var gridDistanceX: Float = 1f
    var gridDistanceY: Float = 1f

    private val lineGraphs: MutableList<PlotData> = emptyList<PlotData>().toMutableList()

    // Allocate here to optimize drawing
    val painter = PlotPainter()

    fun addLineGraph(graph: PlotData) {
        lineGraphs.add(graph)
    }

    fun draw(canvas: Canvas, transform: Transform) {
        painter.initialize(canvas = canvas, transform = transform, style = style)

        painter.drawGrid(gridDistanceX = gridDistanceX, gridDistanceY = gridDistanceY)

        for (line: PlotData in lineGraphs) {
            painter.drawLineGraph(line, paint)
        }
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        )
    }
}