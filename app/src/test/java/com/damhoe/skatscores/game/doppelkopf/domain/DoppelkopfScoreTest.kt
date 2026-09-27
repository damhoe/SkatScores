package com.damhoe.skatscores.game.doppelkopf.domain

import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.KONTRA
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfRoundValue
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import org.junit.Assert.assertEquals
import java.util.UUID
import org.junit.Test

class DoppelkopfScoreTest
{
    private val alice = UUID.randomUUID()
    private val bob = UUID.randomUUID()
    private val carol = UUID.randomUUID()
    private val dan = UUID.randomUUID()

    private fun normal(
        winner: DoppelkopfParty = RE,
        value: DoppelkopfRoundValue = DoppelkopfRoundValue(),
    ) = DoppelkopfScore.Normal(UUID.randomUUID(), setOf(alice, bob), winner, value)

    private fun solo(
        winner: DoppelkopfParty = RE,
        value: DoppelkopfRoundValue = DoppelkopfRoundValue(),
    ) = DoppelkopfScore.Solo(
        UUID.randomUUID(),
        alice,
        DoppelkopfSoloKind.QUEENS,
        winner,
        value,
    )

    @Test
    fun `a plain win is worth one point`()
    {
        assertEquals(1, normal().toPoints())
    }

    @Test
    fun `the levels below 90 are cumulative`()
    {
        // Win + keine 90 + keine 60.
        val value = DoppelkopfRoundValue(winLevel = DoppelkopfWinLevel.NO_60)

        assertEquals(3, normal(value = value).toPoints())
    }

    @Test
    fun `schwarz is worth the win plus all four levels`()
    {
        val value = DoppelkopfRoundValue(winLevel = DoppelkopfWinLevel.SCHWARZ)

        assertEquals(5, normal(value = value).toPoints())
    }

    @Test
    fun `beating the elders is worth an extra point`()
    {
        assertEquals(1, normal(winner = RE).toPoints())
        assertEquals(2, normal(winner = KONTRA).toPoints())
    }

    @Test
    fun `a solo has no elders to beat, so the soloist's opponents get no bonus`()
    {
        assertEquals(1, solo(winner = RE).toPoints())
        assertEquals(1, solo(winner = KONTRA).toPoints())
    }

    @Test
    fun `each announcement is worth two points to whoever wins`()
    {
        val bothCalled = DoppelkopfRoundValue(reAnnounced = true, kontraAnnounced = true)

        // 1 for the win + 2 + 2.
        assertEquals(5, normal(winner = RE, value = bothCalled).toPoints())
        // Plus 1 for "gegen die Alten".
        assertEquals(6, normal(winner = KONTRA, value = bothCalled).toPoints())
    }

    @Test
    fun `an announced level is worth the levels it covers`()
    {
        val value = DoppelkopfRoundValue(
            winLevel = DoppelkopfWinLevel.NO_60,
            absage = DoppelkopfWinLevel.NO_60,
        )

        // 3 for winning under 60, plus 2 for the Absage.
        assertEquals(5, normal(value = value).toPoints())
    }

    @Test
    fun `an announcement counts even when the round was not won that clearly`()
    {
        // The Absage pays even when the level was not reached.
        val value = DoppelkopfRoundValue(
            winLevel = DoppelkopfWinLevel.WON,
            absage = DoppelkopfWinLevel.NO_90,
        )

        assertEquals(2, normal(value = value).toPoints())
    }

    @Test
    fun `extras are netted in favour of the winner`()
    {
        val value = DoppelkopfRoundValue(extraPointsRe = 3, extraPointsKontra = 1)

        assertEquals(2, value.extraPoints(RE))
        assertEquals(-2, value.extraPoints(KONTRA))
    }

    @Test
    fun `a normal round pays the winners out of the losers`()
    {
        val score = normal(winner = RE, value = DoppelkopfRoundValue(reAnnounced = true))
        val value = score.toPoints()

        assertEquals(3, value)
        assertEquals(3, score.pointsFor(alice, value))
        assertEquals(3, score.pointsFor(bob, value))
        assertEquals(-3, score.pointsFor(carol, value))
        assertEquals(-3, score.pointsFor(dan, value))
    }

    @Test
    fun `a soloist wins three times what each opponent loses`()
    {
        val score = solo(winner = RE)
        val value = score.toPoints()

        assertEquals(3, score.pointsFor(alice, value))
        listOf(bob, carol, dan).forEach { assertEquals(-1, score.pointsFor(it, value)) }
    }

    @Test
    fun `a losing soloist pays all three opponents`()
    {
        val score = solo(winner = KONTRA, value = DoppelkopfRoundValue(winLevel = DoppelkopfWinLevel.NO_90))
        val value = score.toPoints()

        assertEquals(2, value)
        assertEquals(-6, score.pointsFor(alice, value))
        listOf(bob, carol, dan).forEach { assertEquals(2, score.pointsFor(it, value)) }
    }

    @Test
    fun `every round adds up to zero across the table`()
    {
        val seats = listOf(alice, bob, carol, dan)
        val rounds = listOf(
            normal(winner = RE),
            normal(winner = KONTRA, value = DoppelkopfRoundValue(kontraAnnounced = true)),
            solo(winner = RE, value = DoppelkopfRoundValue(winLevel = DoppelkopfWinLevel.NO_30)),
            solo(winner = KONTRA),
        )

        rounds.forEach { score ->
            val value = score.toPoints()
            assertEquals(0, seats.sumOf { score.pointsFor(it, value) })
        }
    }
}
