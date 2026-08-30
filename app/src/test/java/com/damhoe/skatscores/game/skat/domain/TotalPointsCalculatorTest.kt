package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.common.WonOrLost.WON
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import com.damhoe.skatscores.player.domain.PlayerName
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class TotalPointsCalculatorTest
{
    private val forehand = Participant.Guest(UUID.randomUUID(), PlayerName("Forehand"))
    private val middlehand = Participant.Guest(UUID.randomUUID(), PlayerName("Middlehand"))
    private val rearhand = Participant.Guest(UUID.randomUUID(), PlayerName("Rearhand"))

    private val participants = SkatParticipants(forehand, middlehand, rearhand)
    private val calculator = TotalPointsCalculator(participants)

    private fun grandOrSuit(
        declarer: Participant,
        wonOrLost: com.damhoe.skatscores.game.common.WonOrLost,
        suit: SkatSuit,
        spitzen: Int,
    ) = SkatScore.GrandOrSuit.create(declarer, wonOrLost, suit, Spitzen(spitzen))

    @Test
    fun `no scores gives one zero per seat`()
    {
        assertArrayEquals(intArrayOf(0, 0, 0), calculator.calculateTotalPoints(emptyList(), false))
    }

    @Test
    fun `a won game credits only the declarer`()
    {
        // Clubs with 2 spitzen -> 12 * (2 + 1) = 36
        val scores = listOf(grandOrSuit(middlehand, WON, SkatSuit.CLUBS, 2))

        assertArrayEquals(intArrayOf(0, 36, 0), calculator.calculateTotalPoints(scores, false))
    }

    @Test
    fun `a lost game debits the declarer twice the game value`()
    {
        val scores = listOf(grandOrSuit(rearhand, LOST, SkatSuit.CLUBS, 2))

        assertArrayEquals(intArrayOf(0, 0, -72), calculator.calculateTotalPoints(scores, false))
    }

    @Test
    fun `scores of several declarers accumulate per seat`()
    {
        val scores = listOf(
            grandOrSuit(forehand, WON, SkatSuit.GRAND, 1),   // 24 * 2 = 48
            grandOrSuit(middlehand, WON, SkatSuit.CLUBS, 2), // 12 * 3 = 36
            SkatScore.Passe.create(),
            grandOrSuit(forehand, LOST, SkatSuit.HEARTS, 1), // -(10 * 2 * 2) = -40
        )

        assertArrayEquals(intArrayOf(8, 36, 0), calculator.calculateTotalPoints(scores, false))
    }

    @Test
    fun `tournament scoring adds 50 to the declarer of a won game`()
    {
        val scores = listOf(grandOrSuit(middlehand, WON, SkatSuit.CLUBS, 2))

        val breakdown = calculator.calculateBreakdown(scores)

        assertEquals(listOf(0, 50, 0), breakdown.map { it.soloBonus })
        assertEquals(listOf(0, 0, 0), breakdown.map { it.againstBonus })
        assertArrayEquals(intArrayOf(0, 86, 0), calculator.calculateTotalPoints(scores, true))
    }

    @Test
    fun `tournament scoring takes 50 from the declarer and gives 40 to each defender`()
    {
        val scores = listOf(grandOrSuit(middlehand, LOST, SkatSuit.CLUBS, 2))

        val breakdown = calculator.calculateBreakdown(scores)

        assertEquals(listOf(0, -50, 0), breakdown.map { it.soloBonus })
        assertEquals(listOf(40, 0, 40), breakdown.map { it.againstBonus })
        assertArrayEquals(intArrayOf(40, -122, 40), calculator.calculateTotalPoints(scores, true))
    }

    @Test
    fun `a passed round pays nobody`()
    {
        val scores = listOf(SkatScore.Passe.create())

        val breakdown = calculator.calculateBreakdown(scores)

        assertArrayEquals(intArrayOf(0, 0, 0), calculator.calculateTotalPoints(scores, true))
        assertEquals(listOf(0, 0, 0), breakdown.map { it.soloBonus })
        assertEquals(listOf(0, 0, 0), breakdown.map { it.againstBonus })
        assertEquals(listOf(0, 0, 0), breakdown.map { it.declarerWins })
        assertEquals(listOf(0, 0, 0), breakdown.map { it.defenderWins })
    }

    @Test
    fun `the breakdown counts games per seat and adds up to the total`()
    {
        val scores = listOf(
            grandOrSuit(forehand, WON, SkatSuit.GRAND, 1),    // 48
            grandOrSuit(middlehand, LOST, SkatSuit.CLUBS, 2), // -72
            SkatScore.Passe.create(),
            grandOrSuit(forehand, LOST, SkatSuit.HEARTS, 1),  // -40
            grandOrSuit(rearhand, WON, SkatSuit.CLUBS, 2),    // 36
        )

        val breakdown = calculator.calculateBreakdown(scores)

        assertEquals(listOf(1, 0, 1), breakdown.map { it.declarerWins })
        assertEquals(listOf(1, 1, 0), breakdown.map { it.declarerLosses })
        assertEquals(listOf(1, 1, 2), breakdown.map { it.defenderWins })
        assertEquals(listOf(8, -72, 36), breakdown.map { it.gameValue })
        assertEquals(listOf(0, -50, 50), breakdown.map { it.soloBonus })
        assertEquals(listOf(40, 40, 80), breakdown.map { it.againstBonus })

        assertArrayEquals(
            breakdown.map { it.total }.toIntArray(),
            calculator.calculateTotalPoints(scores, true)
        )
        assertArrayEquals(
            breakdown.map { it.gameValue }.toIntArray(),
            calculator.calculateTotalPoints(scores, false)
        )
    }

    @Test
    fun `an overbid game counts as a loss for the declarer`()
    {
        val scores = listOf(SkatScore.Overbid.create(forehand, SkatSuit.CLUBS, SkatBid(40)))

        val breakdown = calculator.calculateBreakdown(scores)

        assertEquals(listOf(0, 0, 0), breakdown.map { it.declarerWins })
        assertEquals(listOf(1, 0, 0), breakdown.map { it.declarerLosses })
        assertEquals(listOf(0, 1, 1), breakdown.map { it.defenderWins })
    }

    @Test
    fun `an overbid game counts as lost for the declarer`()
    {
        // Clubs raised above a bid of 40 -> 48, floored at 50 -> -50
        val scores = listOf(
            SkatScore.Overbid.create(forehand, SkatSuit.CLUBS, SkatBid(40))
        )

        assertArrayEquals(intArrayOf(-50, 0, 0), calculator.calculateTotalPoints(scores, false))
        assertArrayEquals(intArrayOf(-100, 40, 40), calculator.calculateTotalPoints(scores, true))
    }

    @Test
    fun `a score of an unknown declarer is ignored instead of shifting the columns`()
    {
        val strangerScore = SkatScore.GrandOrSuit(
            id = UUID.randomUUID(),
            skatParticipant = UUID.randomUUID(),
            wonOrLost = WON,
            suit = SkatSuit.CLUBS,
            spitzen = Spitzen(1),
            options = null,
        )

        assertArrayEquals(
            intArrayOf(0, 0, 0),
            calculator.calculateTotalPoints(listOf(strangerScore), true)
        )
    }

    @Test
    fun `points history starts at zero and grows by one entry per round`()
    {
        val scores = listOf(
            grandOrSuit(forehand, WON, SkatSuit.GRAND, 1),   // 48
            grandOrSuit(forehand, LOST, SkatSuit.HEARTS, 1), // -40
        )

        val history = calculator.createPointsHistory(scores, false)

        assertEquals(3, history.size)
        assertEquals(listOf(0, 48, 8), history[0])
        assertEquals(listOf(0, 0, 0), history[1])
        assertEquals(listOf(0, 0, 0), history[2])
    }
}
