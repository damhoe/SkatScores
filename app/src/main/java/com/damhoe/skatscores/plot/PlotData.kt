package com.damhoe.skatscores.plot

import android.graphics.PointF
import androidx.annotation.ColorInt
import com.damhoe.skatscores.plot.presentation.Transform

data class PlotData(var points: List<PointF>, @ColorInt var color: Int, var label: String) {

    fun toPixels(transform: Transform) = copy().apply {
        points = points.map { p -> PointF(transform.toPixelX(p.x), transform.toPixelY(p.y)) }
    }
}