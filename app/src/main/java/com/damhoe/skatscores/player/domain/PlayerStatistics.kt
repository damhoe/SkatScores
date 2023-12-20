package com.damhoe.skatscores.player.domain

data class PlayerStatistics(
    var listCount: Int,
    var gamesCount: Int,
    var soloPercentage: Double,
    var soloWinPercentage: Double,
    var againstWinPercentage: Double
)