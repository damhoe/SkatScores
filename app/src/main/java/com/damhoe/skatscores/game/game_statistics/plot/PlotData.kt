package com.damhoe.skatscores.game.game_statistics.plot

import android.graphics.PointF
import androidx.annotation.ColorInt
import com.damhoe.skatscores.game.game_statistics.presentation.Transform

data class PlotData(var points: List<PointF>, @ColorInt var color: Int, var label: String) {

    fun toPixels(transform: Transform) = copy().apply {
        points = points.map { p -> PointF(transform.toPixelX(p.x), transform.toPixelY(p.y)) }
    }
}