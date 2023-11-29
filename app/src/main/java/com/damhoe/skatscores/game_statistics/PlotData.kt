package com.damhoe.skatscores.game_statistics

import android.graphics.PointF
import androidx.annotation.ColorInt

data class PlotData(val points: List<PointF>, @ColorInt val color: Int)

fun PlotData.toPixels(transform: Transform) =
    PlotData(
        points = points.map { p ->
            PointF(transform.toPixelX(p.x), transform.toPixelY(p.y))
        }, color = color
    )