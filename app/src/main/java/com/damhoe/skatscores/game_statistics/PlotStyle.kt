package com.damhoe.skatscores.game_statistics

import android.graphics.Color

data class PlotStyle(
    var markerSize: Float = 5f,
    var lineWidth: Float = 1f,
    var gridStrokeWidth: Float = 1f,
    var gridColor: Int = Color.BLACK.hashCode(),
    var tickLabelSize: Float = 10f // dp
)