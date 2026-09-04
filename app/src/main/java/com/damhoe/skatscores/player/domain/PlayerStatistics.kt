package com.damhoe.skatscores.player.domain

/**
 * What a player has done across every list they appear in, in either game.
 *
 * Profiles are shared between Skat and Doppelkopf, so a player's record spans both. The two
 * games measure different things, though - Skat asks how often you declare and win alone,
 * Doppelkopf how often you end up on the winning side - so each keeps its own section and only
 * the counts are added together.
 *
 * The derived values are shares in 0..1, not percentages; formatting is the screen's job.
 */
data class PlayerStatistics(
    val skat: SkatPlayerStatistics = SkatPlayerStatistics(),
    val doppelkopf: DoppelkopfPlayerStatistics = DoppelkopfPlayerStatistics(),
)
{
    /** Lists the player appears in, kept split by game so a screen can name the two. */
    val listCounts: ListCounts
        get() = ListCounts(skat = skat.listsPlayed, doppelkopf = doppelkopf.listsPlayed)

    /** Lists the player appears in, both games together. */
    val totalListsPlayed: Int
        get() = listCounts.total

    /** Rounds played, both games together. */
    val totalRoundsPlayed: Int
        get() = skat.roundsPlayed + doppelkopf.roundsPlayed

    val hasRounds: Boolean
        get() = totalRoundsPlayed > 0
}

/** Each share is guarded by its own denominator, never by a different count. */
internal fun ratio(part: Int, whole: Int): Double =
    if (whole > 0) part.toDouble() / whole else 0.0

data class SkatPlayerStatistics(
    val listsPlayed: Int = 0,
    val roundsPlayed: Int = 0,
    val soloRoundsPlayed: Int = 0,
    val soloRoundsWon: Int = 0,
    val roundsWonAsOpponent: Int = 0,
)
{
    /** Rounds where somebody else declared. */
    val opponentRoundsPlayed: Int
        get() = roundsPlayed - soloRoundsPlayed

    /** How often this player declares, out of all rounds they played. */
    val soloShare: Double
        get() = ratio(soloRoundsPlayed, roundsPlayed)

    /** How often a declared round is won, out of the rounds this player declared. */
    val soloWinRate: Double
        get() = ratio(soloRoundsWon, soloRoundsPlayed)

    /** How often a defended round is won, out of the rounds somebody else declared. */
    val defenderWinRate: Double
        get() = ratio(roundsWonAsOpponent, opponentRoundsPlayed)

    val hasRounds: Boolean
        get() = roundsPlayed > 0
}

data class DoppelkopfPlayerStatistics(
    val listsPlayed: Int = 0,
    val roundsPlayed: Int = 0,
    val roundsWon: Int = 0,
    val soloRoundsPlayed: Int = 0,
    val soloRoundsWon: Int = 0,
    /** Rounds played on the Re side, solos included. */
    val reRoundsPlayed: Int = 0,
    val reRoundsWon: Int = 0,
)
{
    /** How often this player ends a round on the winning side. */
    val winRate: Double
        get() = ratio(roundsWon, roundsPlayed)

    /** How often this player goes alone, out of all rounds they played. */
    val soloShare: Double
        get() = ratio(soloRoundsPlayed, roundsPlayed)

    /** How often a solo is won, out of the solos this player played. */
    val soloWinRate: Double
        get() = ratio(soloRoundsWon, soloRoundsPlayed)

    /** How often a Re round is won, out of the rounds this player held the queens. */
    val reWinRate: Double
        get() = ratio(reRoundsWon, reRoundsPlayed)

    val hasRounds: Boolean
        get() = roundsPlayed > 0
}
