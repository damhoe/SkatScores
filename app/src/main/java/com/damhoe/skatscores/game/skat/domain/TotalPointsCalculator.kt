package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore

data class SeatBreakdown(
    /** Game values of the rounds this seat declared, without bonuses. */
    val gameValue: Int,
    val declarerWins: Int,
    val declarerLosses: Int,
    val defenderWins: Int,
    /** ±50 per declared game. */
    val soloBonus: Int,
    /** 40 per round in [defenderWins]. */
    val againstBonus: Int,
)
{
    val total = gameValue + soloBonus + againstBonus
}

/**
 * Seats are indexed as in [SkatParticipants.asList]. The game value goes to the declarer only.
 * Tournament scoring (Seeger-Fabian) adds ±[BONUS_SOLO] for the declarer and [BONUS_AGAINST]
 * for each defender when the declarer loses.
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

    fun calculateBreakdown(scores: List<SkatScore>): List<SeatBreakdown>
    {
        val gameValue = IntArray(seatCount)
        val declarerWins = IntArray(seatCount)
        val declarerLosses = IntArray(seatCount)
        val defenderWins = IntArray(seatCount)

        for (score in scores)
        {
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

    /** Per seat: 0, then the running total after each round. */
    fun createPointsHistory(
        scores: List<SkatScore>,
        isTournamentScoring: Boolean
    ): List<List<Int>>
    {
        val pointsHistory = List(seatCount) { mutableListOf(0) }

        for (score in scores)
        {
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

    /** Null for a passed round or a declarer no longer at the table. */
    private fun seatOf(score: SkatScore): Int? =
        score.declarerId?.let { participants.seatOf(it) }
}
