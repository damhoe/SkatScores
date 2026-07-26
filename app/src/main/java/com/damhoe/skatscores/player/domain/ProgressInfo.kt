package com.damhoe.skatscores.player.domain

data class ProgressInfo(
    var gamesCount: Int,
    var openGamesCount: Int
)
{
    fun toPercent(): Double
    {
        return 100.0 * (1.0 - openGamesCount.toDouble() / gamesCount)
    }
}
