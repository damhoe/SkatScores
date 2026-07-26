package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore

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
    ): IntArray = scores.sumPerSeat { pointsForScore(it, isTournamentScoring) }

    fun calculateWinBonus(scores: List<SkatScore>): IntArray =
        scores.sumPerSeat(::soloBonusForScore)

    fun calculateLossOfOthersBonus(scores: List<SkatScore>): IntArray =
        scores.sumPerSeat(::againstBonusForScore)

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
            val points = pointsForScore(score, isTournamentScoring)
            pointsHistory.forEachIndexed { seat, seatHistory ->
                seatHistory.add(seatHistory.last() + points[seat])
            }
        }

        return pointsHistory
    }

    private fun pointsForScore(
        score: SkatScore,
        isTournamentScoring: Boolean
    ): IntArray
    {
        val points = gameValueForScore(score)

        if (isTournamentScoring)
        {
            val soloBonus = soloBonusForScore(score)
            val againstBonus = againstBonusForScore(score)
            for (seat in points.indices)
            {
                points[seat] += soloBonus[seat] + againstBonus[seat]
            }
        }

        return points
    }

    /** The game value of the round, credited to the declarer. */
    private fun gameValueForScore(score: SkatScore): IntArray
    {
        val points = IntArray(seatCount)
        val declarerSeat = seatOf(score) ?: return points

        points[declarerSeat] = score.toPoints()

        return points
    }

    private fun soloBonusForScore(score: SkatScore): IntArray
    {
        val points = IntArray(seatCount)
        val declarerSeat = seatOf(score) ?: return points

        points[declarerSeat] = if (score.result == LOST) -BONUS_SOLO else BONUS_SOLO

        return points
    }

    private fun againstBonusForScore(score: SkatScore): IntArray
    {
        val points = IntArray(seatCount)
        val declarerSeat = seatOf(score) ?: return points

        if (score.result != LOST)
        {
            return points
        }

        for (seat in points.indices)
        {
            if (seat != declarerSeat)
            {
                points[seat] = BONUS_AGAINST
            }
        }

        return points
    }

    /**
     * Seat of the declarer, or null for a passed round and for scores whose declarer is
     * not at this table any more.
     */
    private fun seatOf(score: SkatScore): Int? =
        score.declarerId?.let { participants.seatOf(it) }

    private fun List<SkatScore>.sumPerSeat(pointsForScore: (SkatScore) -> IntArray): IntArray =
        fold(IntArray(seatCount)) { total, score ->
            val points = pointsForScore(score)
            for (seat in total.indices)
            {
                total[seat] += points[seat]
            }
            total
        }
}
