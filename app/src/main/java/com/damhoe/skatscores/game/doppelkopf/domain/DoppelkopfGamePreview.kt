package com.damhoe.skatscores.game.doppelkopf.domain

import com.damhoe.skatscores.game.common.GameType
import com.damhoe.skatscores.game.common.ListPreview
import com.damhoe.skatscores.game.common.Title
import java.time.Instant
import java.util.UUID

/**
 * Everything the home screen needs about one Doppelkopf list.
 *
 * Seat order of [playerNames] and [totals] is the table order of
 * [DoppelkopfParticipants.asList].
 */
data class DoppelkopfGamePreview(
    val title: Title,
    val playedAt: Instant,
    val playerNames: List<String>,
    val gameId: UUID,
    val participants: DoppelkopfParticipants? = null,
    val totals: List<Int> = emptyList(),
    val roundsPlayed: Int = 0,
    val totalRounds: Int = 0,
    val dealerPosition: Int = 0,
    val scoringMode: DoppelkopfScoringMode = DoppelkopfGameDefaults.ScoringMode,
)
{
    fun toListPreview() = ListPreview(
        gameType = GameType.DOPPELKOPF,
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
        fun mapFrom(game: DoppelkopfGame) = DoppelkopfGamePreview(
            title = game.title,
            playedAt = game.playedAt,
            playerNames = game.participants.asList().map { it.displayName },
            gameId = game.id,
            participants = game.participants,
            totals = game.calculateTotalPoints().toList(),
            roundsPlayed = game.scores.size,
            totalRounds = game.settings.roundCount.value,
            dealerPosition = game.dealerPosition,
            scoringMode = game.settings.scoringMode,
        )
    }
}
