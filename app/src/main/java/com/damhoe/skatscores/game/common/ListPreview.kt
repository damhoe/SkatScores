package com.damhoe.skatscores.game.common

import java.time.Instant
import java.util.UUID

/** What the home screen shows of a list, common to both games. */
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
    val isRunning: Boolean
        get() = roundsPlayed < totalRounds

    /** In 0..1. */
    val progress: Float
        get() = if (totalRounds <= 0) 0f else (roundsPlayed.toFloat() / totalRounds).coerceIn(0f, 1f)

    val dealerName: String?
        get() = playerNames.getOrNull(dealerPosition)

    /** Null before anything has been scored. */
    val leaderPosition: Int?
        get() = if (roundsPlayed == 0) null
        else totals.withIndex().maxByOrNull { it.value }?.index

    val leaderTotal: Int?
        get() = leaderPosition?.let { totals.getOrNull(it) }
}
