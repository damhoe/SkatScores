package com.damhoe.skatscores.plot.domain

import android.graphics.PointF
import androidx.annotation.ColorInt

data class LineGraphData(
    var points: List<PointF>,
    @ColorInt var color: Int,
    var label: String,
)
{
    fun toPixels(
        transform: Transform
    ) = copy().apply {
        points = points.map { p ->
            PointF(
                transform.toPixelX(
                    p.x),
                transform.toPixelY(
                    p.y))
        }
    }
}