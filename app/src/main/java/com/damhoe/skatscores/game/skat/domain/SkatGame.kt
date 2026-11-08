package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.common.dealerSeat
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import java.time.Instant
import java.time.Instant.now
import java.util.UUID

data class SkatGame(
    val id: UUID,
    val title: Title,
    val playedAt: Instant,
    val settings: SkatSettings,
    val participants: SkatParticipants,
    val scores: List<SkatScore> = emptyList(),
)
{
    private val totalPointsCalculator = TotalPointsCalculator(participants)

    val isTournamentScoring = settings.scoringMode == SkatScoringMode.TOURNAMENT

    val dealerPosition = dealerSeat(roundsPlayed = scores.size, participants.asList().size)

    companion object
    {
        fun create(
            title: Title,
            settings: SkatSettings,
            players: SkatParticipants,
        ): SkatGame
        {
            return SkatGame(
                UUID.randomUUID(),
                title,
                now(),
                settings,
                players,
            )
        }
    }

    fun calculateTotalPoints() =
        totalPointsCalculator.calculateTotalPoints(scores, isTournamentScoring)

    fun calculateBreakdown() = totalPointsCalculator.calculateBreakdown(scores)

    fun createPointsHistory() =
        totalPointsCalculator.createPointsHistory(scores, isTournamentScoring)

    fun addScore(score: SkatScore): AddedScoreToGame
    {
        val newScores = this.scores.plus(score)
        val updatedGameInstance = this.copy(scores = newScores)

        return AddedScoreToGame(
            score = score,
            round = scores.size,
            updatedSkatGame = updatedGameInstance,
        )
    }

    fun removeLastScore() = this.copy(scores = scores.dropLast(1))

    fun removeScore(scoreId: UUID) = this.copy(scores = scores.filterNot { it.id == scoreId })

    fun updateScore(score: SkatScore) =
        this.copy(scores = scores.map { if (it.id == score.id) score else it })
}

