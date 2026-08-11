package com.damhoe.skatscores.player.domain

/**
 * What a player has done across every list they appear in.
 *
 * The derived values are shares in 0..1, not percentages; formatting is the screen's job.
 */
data class PlayerStatistics(
    val totalGamesPlayed: Int,
    val totalRoundsPlayed: Int,
    val soloRoundsPlayed: Int,
    val soloRoundsWon: Int,
    val roundsWonAsOpponent: Int
)
{
    /** Rounds where somebody else declared. */
    val opponentRoundsPlayed: Int
        get() = totalRoundsPlayed - soloRoundsPlayed

    /** How often this player declares, out of all rounds they played. */
    val soloShare: Double
        get() = ratio(soloRoundsPlayed, totalRoundsPlayed)

    /** How often a declared round is won, out of the rounds this player declared. */
    val soloWinRate: Double
        get() = ratio(soloRoundsWon, soloRoundsPlayed)

    /** How often a defended round is won, out of the rounds somebody else declared. */
    val defenderWinRate: Double
        get() = ratio(roundsWonAsOpponent, opponentRoundsPlayed)

    val hasRounds: Boolean
        get() = totalRoundsPlayed > 0

    /** Each share is guarded by its own denominator, never by a different count. */
    private fun ratio(part: Int, whole: Int): Double =
        if (whole > 0) part.toDouble() / whole else 0.0
}
