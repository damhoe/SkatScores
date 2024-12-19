package com.damhoe.skatscores.plot.styles

import android.graphics.Color

data class PlotStyle(
    var markerSize: Float = 4f,
    var lineWidth: Float = 1f,
    var gridStrokeWidth: Float = 1f,
    var gridColor: Int = Color.BLACK.hashCode(),
    var tickLabelSize: Float = 10f, // dp
    var labelSize: Float = 14f, // dp
    var labelMarkerSize: Float = 12f, // dp
    var tickLabelColor: Int = Color.BLACK,
    var legendColor: Int = Color.BLACK
)