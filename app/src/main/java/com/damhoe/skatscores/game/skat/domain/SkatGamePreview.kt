package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.GameType
import com.damhoe.skatscores.game.common.ListPreview
import com.damhoe.skatscores.game.common.Title
import java.time.Instant
import java.util.UUID

/**
 * Everything the home screen needs about one list without loading the whole game.
 *
 * Seat order of [playerNames] and [totals] is the table order of [SkatParticipants.asList].
 */
data class SkatGamePreview(
    val title: Title,
    val playedAt: Instant,
    val playerNames: List<String>,
    val gameId: UUID,
    val participants: SkatParticipants? = null,
    val totals: List<Int> = emptyList(),
    val roundsPlayed: Int = 0,
    val totalRounds: Int = 0,
    val dealerPosition: Int = 0,
    val scoringMode: SkatScoringMode = SkatScoringMode.CLASSIC,
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

    /** The game-agnostic view of this list, for the home screen. */
    fun toListPreview() = ListPreview(
        gameType = GameType.SKAT,
        gameId = gameId,
        title = title,
        playedAt = playedAt,
        playerNames = playerNames,
        totals = totals,
        roundsPlayed = roundsPlayed,
        totalRounds = totalRounds,
        dealerPosition = dealerPosition,
    )

    companion object
    {
        fun mapFrom(skatGame: SkatGame): SkatGamePreview
        {
            return SkatGamePreview(
                title = skatGame.title,
                playedAt = skatGame.playedAt,
                playerNames = skatGame.participants.asList().map { it.displayName },
                gameId = skatGame.id,
                participants = skatGame.participants,
                totals = skatGame.calculateTotalPoints().toList(),
                roundsPlayed = skatGame.scores.size,
                totalRounds = skatGame.settings.roundCount.value,
                dealerPosition = skatGame.dealerPosition,
                scoringMode = skatGame.settings.scoringMode,
            )
        }
    }
}
