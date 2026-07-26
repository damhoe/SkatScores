package com.damhoe.skatscores.player.domain

data class PlayerStatistics(
    val totalGamesPlayed: Int,
    val totalRoundsPlayed: Int,
    val soloRoundsPlayed: Int,
    val soloRoundsWon: Int,
    val roundsWonAsOpponent: Int
)
{
    val soloPercentage: Double =
        if (totalGamesPlayed > 0)
        {
            soloRoundsPlayed.toDouble() / totalRoundsPlayed
        } else
        {
            0.0
        }

    val soloWinPercentage: Double =
        if (soloRoundsPlayed > 0)
        {
            soloRoundsWon.toDouble() / totalRoundsPlayed
        } else
        {
            0.0
        }

    val opponentRoundsPlayed = totalRoundsPlayed - soloRoundsPlayed

    val opponentWinPercentage: Double =
        if (opponentRoundsPlayed > 0)
        {
            roundsWonAsOpponent.toDouble() / opponentRoundsPlayed
        } else
        {
            0.0
        }
}