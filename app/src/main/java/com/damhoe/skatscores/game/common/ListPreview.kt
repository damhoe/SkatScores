package com.damhoe.skatscores.game.common

import java.time.Instant
import java.util.UUID

/**
 * One list as the home screen shows it, whichever game it is scored with.
 *
 * Skat and Doppelkopf keep their own previews, because each carries the participants and
 * settings of its own game. This is what they have in common: a name, a date, a column of
 * names with a running total under each, and how far the list has got. The home screen shows
 * lists of both games side by side, so it works with this rather than with either of them.
 */
data class ListPreview(
    val gameType: GameType,
    val gameId: UUID,
    val title: Title,
    val playedAt: Instant,
    val playerNames: List<String>,
    val totals: List<Int>,
    val roundsPlayed: Int,
    val totalRounds: Int,
    val dealerPosition: Int,
)
{
    /** A list is running until its agreed number of rounds has been played. */
    val isRunning: Boolean
        get() = roundsPlayed < totalRounds

    /** Fraction of the agreed rounds that has been played, in 0..1. */
    val progress: Float
        get() = if (totalRounds <= 0) 0f else (roundsPlayed.toFloat() / totalRounds).coerceIn(0f, 1f)

    /** Seat that deals the next round. */
    val dealerName: String?
        get() = playerNames.getOrNull(dealerPosition)

    /** Seat with the highest total, or null before anything has been scored. */
    val leaderPosition: Int?
        get() = if (roundsPlayed == 0) null
        else totals.withIndex().maxByOrNull { it.value }?.index

    /** The leading total, or null before anything has been scored. */
    val leaderTotal: Int?
        get() = leaderPosition?.let { totals.getOrNull(it) }
}
