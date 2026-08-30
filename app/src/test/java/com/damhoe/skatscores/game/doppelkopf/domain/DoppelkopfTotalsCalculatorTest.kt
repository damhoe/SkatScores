package com.damhoe.skatscores.game.doppelkopf.domain

import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.KONTRA
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfRoundValue
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import com.damhoe.skatscores.player.domain.PlayerName
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import java.util.UUID
import org.junit.Test

class DoppelkopfTotalsCalculatorTest
{
    private val seats = List(4) { seat ->
        Participant.Guest(UUID.randomUUID(), PlayerName("P${seat + 1}"))
    }
    private val participants = DoppelkopfParticipants(seats)
    private val calculator = DoppelkopfTotalsCalculator(participants)

    private fun normal(
        reSeats: List<Int>,
        winner: DoppelkopfParty,
        value: DoppelkopfRoundValue = DoppelkopfRoundValue(),
    ) = DoppelkopfScore.Normal(
        UUID.randomUUID(),
        reSeats.map { seats[it].id }.toSet(),
        winner,
        value,
    )

    private fun solo(
        soloistSeat: Int,
        winner: DoppelkopfParty,
        value: DoppelkopfRoundValue = DoppelkopfRoundValue(),
    ) = DoppelkopfScore.Solo(
        UUID.randomUUID(),
        seats[soloistSeat].id,
        DoppelkopfSoloKind.SUIT,
        winner,
        value,
    )

    @Test
    fun `totals accumulate over the rounds`()
    {
        val scores = listOf(
            normal(reSeats = listOf(0, 1), winner = RE),
            normal(reSeats = listOf(0, 2), winner = KONTRA),
        )

        // Round 1: Re is seats 0 and 1, so they take 1 each and seats 2 and 3 give 1 each.
        // Round 2: Re is seats 0 and 2, and Kontra - seats 1 and 3 - wins it for 2, because
        // beating the elders is worth a point on top of the win.
        assertArrayEquals(
            intArrayOf(-1, 3, -3, 1),
            calculator.calculateTotalPoints(scores, countsExtraPoints = false)
        )
    }

    @Test
    fun `extras only count when the list is scored with them`()
    {
        val scores = listOf(
            normal(
                reSeats = listOf(0, 1),
                winner = RE,
                value = DoppelkopfRoundValue(extraPointsRe = 2),
            )
        )

        assertArrayEquals(
            intArrayOf(1, 1, -1, -1),
            calculator.calculateTotalPoints(scores, countsExtraPoints = false)
        )
        assertArrayEquals(
            intArrayOf(3, 3, -3, -3),
            calculator.calculateTotalPoints(scores, countsExtraPoints = true)
        )
    }

    @Test
    fun `the table always adds up to zero`()
    {
        val scores = listOf(
            normal(reSeats = listOf(0, 3), winner = KONTRA),
            solo(soloistSeat = 2, winner = RE),
            solo(soloistSeat = 1, winner = KONTRA),
            normal(
                reSeats = listOf(1, 2),
                winner = RE,
                value = DoppelkopfRoundValue(
                    winLevel = DoppelkopfWinLevel.NO_30,
                    reAnnounced = true,
                ),
            ),
        )

        val totals = calculator.calculateTotalPoints(scores, countsExtraPoints = true)

        assertEquals(0, totals.sum())
    }

    @Test
    fun `a record counts the rounds a seat won, lost and played alone`()
    {
        val scores = listOf(
            normal(reSeats = listOf(0, 1), winner = RE),
            solo(soloistSeat = 0, winner = RE),
            solo(soloistSeat = 0, winner = KONTRA),
        )

        val records = calculator.calculateRecords(scores, countsExtraPoints = false)

        assertEquals(2, records[0].roundsWon)
        assertEquals(1, records[0].roundsLost)
        assertEquals(2, records[0].solosPlayed)
        assertEquals(1, records[0].solosWon)

        // Seat 3 was never Re, so its two wins are the rounds the soloist lost.
        assertEquals(1, records[3].roundsWon)
        assertEquals(0, records[3].solosPlayed)
    }

    @Test
    fun `the history starts at zero and gains one entry per round`()
    {
        val scores = listOf(
            normal(reSeats = listOf(0, 1), winner = RE),
            normal(reSeats = listOf(0, 1), winner = RE),
        )

        val history = calculator.createPointsHistory(scores, countsExtraPoints = false)

        assertEquals(4, history.size)
        assertEquals(listOf(0, 1, 2), history[0])
        assertEquals(listOf(0, -1, -2), history[2])
    }

    @Test
    fun `the last history entry matches the total`()
    {
        val scores = listOf(
            solo(soloistSeat = 1, winner = RE, value = DoppelkopfRoundValue(reAnnounced = true)),
            normal(reSeats = listOf(2, 3), winner = KONTRA),
        )

        val totals = calculator.calculateTotalPoints(scores, countsExtraPoints = true)
        val history = calculator.createPointsHistory(scores, countsExtraPoints = true)

        history.forEachIndexed { seat, seatHistory ->
            assertEquals(totals[seat], seatHistory.last())
        }
    }
}
