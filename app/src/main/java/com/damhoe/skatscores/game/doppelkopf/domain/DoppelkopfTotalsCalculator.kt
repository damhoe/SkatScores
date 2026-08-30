package com.damhoe.skatscores.game.doppelkopf.domain

import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore

/**
 * What one seat's total is made of.
 *
 * The counts are what the score board shows under each total: how a seat has fared on either
 * side of the table, and how often it has gone alone.
 */
data class DoppelkopfSeatRecord(
    val roundsWon: Int,
    val roundsLost: Int,
    val solosPlayed: Int,
    val solosWon: Int,
    val total: Int,
)

/**
 * Aggregates the rounds of a Doppelkopf list into one point value per seat.
 *
 * Seats are indexed in the order of [DoppelkopfParticipants.asList], which is also the column
 * order of the score board. Every round is zero-sum: what the winning party gains, the losing
 * party gives up, and a soloist wins or loses three times what a single opponent does.
 *
 * With [DoppelkopfScoringMode.DETAILED] the extras collected during play are added to the
 * value of the round, netted in favour of whoever won it.
 */
class DoppelkopfTotalsCalculator(
    private val participants: DoppelkopfParticipants,
)
{
    private val seatCount = participants.asList().size

    /** What a single round came to, including the extras if the list counts them. */
    fun valueOf(score: DoppelkopfScore, countsExtraPoints: Boolean): Int
    {
        val base = score.toPoints()
        return if (countsExtraPoints) base + score.value.extraPoints(score.winner) else base
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

    /** One entry per seat, in the column order of the score board. */
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

    /**
     * Running totals per seat, starting at 0 before the first round, with one entry
     * appended per played round.
     */
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
