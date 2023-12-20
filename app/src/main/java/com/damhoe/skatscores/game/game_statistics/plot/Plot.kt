package com.damhoe.skatscores.game.game_statistics.plot

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.damhoe.skatscores.game.game_statistics.plot.styles.PlotStyle
import com.damhoe.skatscores.game.game_statistics.presentation.Transform
import com.damhoe.skatscores.game.game_statistics.presentation.GraphicUtils.dpToPx

class Plot(val bounds: RectF) {

    val style: PlotStyle = PlotStyle().apply {
        markerSize = dpToPx(4f).toFloat()
        lineWidth = dpToPx(2f).toFloat()
        gridStrokeWidth = dpToPx(1f).toFloat()
        gridColor = Color.GRAY.hashCode()
        tickLabelSize = 11f
    }

    val paint: Paint = Paint()

    var gridDistanceX: Float = 1f
    var gridDistanceY: Float = 1f

    var lineGraphs: MutableList<PlotData> = mutableListOf()

    // Allocate here to optimize drawing
    private val painter = PlotPainter()

    fun draw(canvas: Canvas, transform: Transform) {
        painter.initialize(canvas = canvas, transform = transform, style = style)

        gridDistanceY = transform.viewportHeight / transform.height * 100

        painter.drawGrid(gridDistanceX = gridDistanceX, gridDistanceY = gridDistanceY)

        for (line: PlotData in lineGraphs) {
            painter.drawLineGraph(line, paint)
        }

        painter.drawLabels(lineGraphs)
    }
}