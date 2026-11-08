package com.damhoe.skatscores.game.doppelkopf.domain

import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore

data class DoppelkopfSeatRecord(
    val roundsWon: Int,
    val roundsLost: Int,
    val solosPlayed: Int,
    val solosWon: Int,
    val total: Int,
)

/** Seats are indexed as in [DoppelkopfParticipants.asList]; every round is zero-sum. */
class DoppelkopfTotalsCalculator(
    private val participants: DoppelkopfParticipants,
)
{
    private val seatCount = participants.asList().size

    /** Extras never count in a solo (DDV). */
    fun valueOf(score: DoppelkopfScore, countsExtraPoints: Boolean): Int
    {
        val base = score.toPoints()
        val countsExtras = countsExtraPoints && !score.isSolo
        return if (countsExtras) base + score.value.extraPoints(score.winner) else base
    }

    fun calculateTotalPoints(
        scores: List<DoppelkopfScore>,
        countsExtraPoints: Boolean,
    ): IntArray
    {
        val totals = IntArray(seatCount)

        for (score in scores)
        {
            val value = valueOf(score, countsExtraPoints)
            participants.asList().forEachIndexed { seat, participant ->
                totals[seat] += score.pointsFor(participant.id, value)
            }
        }

        return totals
    }

    fun calculateRecords(
        scores: List<DoppelkopfScore>,
        countsExtraPoints: Boolean,
    ): List<DoppelkopfSeatRecord>
    {
        val totals = calculateTotalPoints(scores, countsExtraPoints)
        val won = IntArray(seatCount)
        val lost = IntArray(seatCount)
        val solosPlayed = IntArray(seatCount)
        val solosWon = IntArray(seatCount)

        for (score in scores)
        {
            participants.asList().forEachIndexed { seat, participant ->
                val party = score.partyOf(participant.id) ?: return@forEachIndexed

                if (party == score.winner) won[seat]++ else lost[seat]++

                if (participant.id == score.soloistId)
                {
                    solosPlayed[seat]++
                    if (party == score.winner) solosWon[seat]++
                }
            }
        }

        return List(seatCount) { seat ->
            DoppelkopfSeatRecord(
                roundsWon = won[seat],
                roundsLost = lost[seat],
                solosPlayed = solosPlayed[seat],
                solosWon = solosWon[seat],
                total = totals[seat],
            )
        }
    }

    /** Per seat: 0, then the running total after each round. */
    fun createPointsHistory(
        scores: List<DoppelkopfScore>,
        countsExtraPoints: Boolean,
    ): List<List<Int>>
    {
        val pointsHistory = List(seatCount) { mutableListOf(0) }

        for (score in scores)
        {
            val value = valueOf(score, countsExtraPoints)
            participants.asList().forEachIndexed { seat, participant ->
                val history = pointsHistory[seat]
                history.add(history.last() + score.pointsFor(participant.id, value))
            }
        }

        return pointsHistory
    }
}
