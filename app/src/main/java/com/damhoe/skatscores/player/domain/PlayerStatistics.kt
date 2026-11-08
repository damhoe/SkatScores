package com.damhoe.skatscores.player.domain

/** Rates are shares in 0..1, not percentages. */
data class PlayerStatistics(
    val skat: SkatPlayerStatistics = SkatPlayerStatistics(),
    val doppelkopf: DoppelkopfPlayerStatistics = DoppelkopfPlayerStatistics(),
)
{
    val listCounts: ListCounts
        get() = ListCounts(skat = skat.listsPlayed, doppelkopf = doppelkopf.listsPlayed)

    val totalListsPlayed: Int
        get() = listCounts.total

    val totalRoundsPlayed: Int
        get() = skat.roundsPlayed + doppelkopf.roundsPlayed

    val hasRounds: Boolean
        get() = totalRoundsPlayed > 0
}

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
    val opponentRoundsPlayed: Int
        get() = roundsPlayed - soloRoundsPlayed

    /** Of all rounds played. */
    val soloShare: Double
        get() = ratio(soloRoundsPlayed, roundsPlayed)

    /** Of the rounds this player declared. */
    val soloWinRate: Double
        get() = ratio(soloRoundsWon, soloRoundsPlayed)

    /** Of the rounds somebody else declared. */
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
    /** Solos included. */
    val reRoundsPlayed: Int = 0,
    val reRoundsWon: Int = 0,
)
{
    val winRate: Double
        get() = ratio(roundsWon, roundsPlayed)

    /** Of all rounds played. */
    val soloShare: Double
        get() = ratio(soloRoundsPlayed, roundsPlayed)

    val soloWinRate: Double
        get() = ratio(soloRoundsWon, soloRoundsPlayed)

    val reWinRate: Double
        get() = ratio(reRoundsWon, reRoundsPlayed)

    val hasRounds: Boolean
        get() = roundsPlayed > 0
}
