package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.GameType
import com.damhoe.skatscores.game.common.ListPreview
import com.damhoe.skatscores.game.common.Title
import java.time.Instant
import java.util.UUID

/** [playerNames] and [totals] are in the seat order of [SkatParticipants.asList]. */
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
