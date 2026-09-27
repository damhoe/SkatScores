package com.damhoe.skatscores.game.doppelkopf.domain

import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.common.dealerSeat
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import java.time.Instant
import java.time.Instant.now
import java.util.UUID

data class DoppelkopfGame(
    val id: UUID,
    val title: Title,
    val playedAt: Instant,
    val settings: DoppelkopfSettings,
    val participants: DoppelkopfParticipants,
    val scores: List<DoppelkopfScore> = emptyList(),
)
{
    private val totalsCalculator = DoppelkopfTotalsCalculator(participants)

    val countsExtraPoints = settings.countsExtraPoints

    val dealerPosition = dealerSeat(roundsPlayed = scores.size, participants.asList().size)

    companion object
    {
        fun create(
            title: Title,
            settings: DoppelkopfSettings,
            participants: DoppelkopfParticipants,
        ) = DoppelkopfGame(
            UUID.randomUUID(),
            title,
            now(),
            settings,
            participants,
        )
    }

    fun calculateTotalPoints() = totalsCalculator.calculateTotalPoints(scores, countsExtraPoints)

    fun calculateRecords() = totalsCalculator.calculateRecords(scores, countsExtraPoints)

    fun createPointsHistory() = totalsCalculator.createPointsHistory(scores, countsExtraPoints)

    fun valueOf(score: DoppelkopfScore) = totalsCalculator.valueOf(score, countsExtraPoints)

    fun addScore(score: DoppelkopfScore): AddedScoreToDoppelkopfGame
    {
        val newScores = this.scores.plus(score)

        return AddedScoreToDoppelkopfGame(
            score = score,
            round = scores.size,
            updatedGame = this.copy(scores = newScores),
        )
    }

    fun removeLastScore() = this.copy(scores = scores.dropLast(1))

    fun removeScore(scoreId: UUID) = this.copy(scores = scores.filterNot { it.id == scoreId })

    fun updateScore(score: DoppelkopfScore) =
        this.copy(scores = scores.map { if (it.id == score.id) score else it })
}

data class AddedScoreToDoppelkopfGame(
    val score: DoppelkopfScore,
    val round: Int,
    val updatedGame: DoppelkopfGame,
)
