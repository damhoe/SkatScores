package com.damhoe.skatscores.game.game_statistics.plot.styles

import android.graphics.Color

data class GridStyle(
    var gridLineWidth: Float = 1f,
    var gridColor: Int = Color.BLACK,
    var tickLabelSize: Float = 10f, // dp
    var tickLabelColor: Int = Color.BLACK,
    var maxVerticalLines: Int = 5,
    var maxHorizontalLines: Int = 5,
    var normalizedIncreaseVertical: Float = 0.5f,
    var normalizedIncreaseHorizontal: Float = 0.5f
)