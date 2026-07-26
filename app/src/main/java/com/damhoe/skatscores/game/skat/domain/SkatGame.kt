package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.Title
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

    private val isTournamentScoring = settings.scoringMode == SkatScoringMode.TOURNAMENT

    val dealerPosition = calculateDealerPosition()

    private fun calculateDealerPosition(): Int
    {
        val firstDealerPosition = 0
        val currentRound: Int = scores.size
        val playerCount = 3
        return (firstDealerPosition - 1 + currentRound) % playerCount
    }

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

    fun calculateWinBonus() = totalPointsCalculator.calculateWinBonus(scores)

    fun calculateLossOfOthersBonus() = totalPointsCalculator.calculateLossOfOthersBonus(scores)

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

    fun updateScore(score: SkatScore) =
        this.copy(scores = scores.map { if (it.id == score.id) score else it })
}

