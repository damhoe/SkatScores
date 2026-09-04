package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore

/**
 * What one seat's total is made of.
 *
 * The parts are what the score board shows when the tournament breakdown is expanded, and
 * they add up to [total] by construction, so the board can never disagree with the sum
 * printed above it.
 */
data class SeatBreakdown(
    /** Sum of the game values of the rounds this seat declared - no bonuses. */
    val gameValue: Int,
    val declarerWins: Int,
    val declarerLosses: Int,
    /** Rounds this seat defended and the declarer lost. */
    val defenderWins: Int,
    /** ±50 per declared game, won or lost. */
    val soloBonus: Int,
    /** 40 per round in [defenderWins]. */
    val againstBonus: Int,
)
{
    val total = gameValue + soloBonus + againstBonus
}

/**
 * Aggregates the scores of a game into one point value per seat.
 *
 * Seats are indexed in the order of [SkatParticipants.asList], which is also the column
 * order of the score board. The game value of a round always goes to the declarer only.
 *
 * With [SkatScoringMode.TOURNAMENT] (Seeger-Fabian) two bonuses are added on top:
 * the declarer gets [BONUS_SOLO] for a won and -[BONUS_SOLO] for a lost game, and every
 * defender gets [BONUS_AGAINST] whenever the declarer loses.
 */
class TotalPointsCalculator(
    private val participants: SkatParticipants,
)
{
    companion object
    {
        private const val BONUS_AGAINST = 40
        private const val BONUS_SOLO = 50
    }

    private val seatCount = participants.asList().size

    fun calculateTotalPoints(
        scores: List<SkatScore>,
        isTournamentScoring: Boolean
    ): IntArray = calculateBreakdown(scores).toPoints(isTournamentScoring)

    /** One entry per seat, in the column order of the score board. */
    fun calculateBreakdown(scores: List<SkatScore>): List<SeatBreakdown>
    {
        val gameValue = IntArray(seatCount)
        val declarerWins = IntArray(seatCount)
        val declarerLosses = IntArray(seatCount)
        val defenderWins = IntArray(seatCount)

        for (score in scores)
        {
            // Passed rounds, and rounds whose declarer has left the table, pay nobody.
            val declarerSeat = seatOf(score) ?: continue

            gameValue[declarerSeat] += score.toPoints()

            if (score.result == LOST)
            {
                declarerLosses[declarerSeat]++
                for (seat in 0 until seatCount)
                {
                    if (seat != declarerSeat) defenderWins[seat]++
                }
            }
            else
            {
                declarerWins[declarerSeat]++
            }
        }

        return List(seatCount) { seat ->
            SeatBreakdown(
                gameValue = gameValue[seat],
                declarerWins = declarerWins[seat],
                declarerLosses = declarerLosses[seat],
                defenderWins = defenderWins[seat],
                soloBonus = (declarerWins[seat] - declarerLosses[seat]) * BONUS_SOLO,
                againstBonus = defenderWins[seat] * BONUS_AGAINST,
            )
        }
    }

    /**
     * Running totals per seat, starting at 0 before the first round, with one entry
     * appended per played round.
     */
    fun createPointsHistory(
        scores: List<SkatScore>,
        isTournamentScoring: Boolean
    ): List<List<Int>>
    {
        val pointsHistory = List(seatCount) { mutableListOf(0) }

        for (score in scores)
        {
            // One round at a time through the same aggregation the totals use.
            val points = calculateBreakdown(listOf(score)).toPoints(isTournamentScoring)
            pointsHistory.forEachIndexed { seat, seatHistory ->
                seatHistory.add(seatHistory.last() + points[seat])
            }
        }

        return pointsHistory
    }

    private fun List<SeatBreakdown>.toPoints(isTournamentScoring: Boolean): IntArray =
        IntArray(size) { seat ->
            this[seat].let { if (isTournamentScoring) it.total else it.gameValue }
        }

    /**
     * Seat of the declarer, or null for a passed round and for scores whose declarer is
     * not at this table any more.
     */
    private fun seatOf(score: SkatScore): Int? =
        score.declarerId?.let { participants.seatOf(it) }
}
