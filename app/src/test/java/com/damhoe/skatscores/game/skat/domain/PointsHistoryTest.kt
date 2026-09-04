package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.common.WonOrLost.WON
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import com.damhoe.skatscores.player.domain.PlayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID
import kotlin.math.max

/**
 * The shape the game graph relies on: one running-total series per seat, aligned with the
 * score board columns, plus the vertical range the chart derives from it.
 */
class PointsHistoryTest
{
    private val forehand = Participant.Guest(UUID.randomUUID(), PlayerName("Marlon"))
    private val middlehand = Participant.Guest(UUID.randomUUID(), PlayerName("Evi"))
    private val rearhand = Participant.Guest(UUID.randomUUID(), PlayerName("Daniel"))

    private fun game(scores: List<SkatScore> = emptyList()) = SkatGame(
        id = UUID.randomUUID(),
        title = Title("Stammtisch"),
        playedAt = Instant.EPOCH,
        settings = SkatSettings(SkatRoundCount(12), SkatScoringMode.CLASSIC),
        participants = SkatParticipants(forehand, middlehand, rearhand),
        scores = scores,
    )

    private fun wonBy(declarer: Participant, suit: SkatSuit, spitzen: Int) =
        SkatScore.GrandOrSuit.create(declarer, WON, suit, Spitzen(spitzen))

    @Test
    fun `history has one series per seat, each starting at zero`()
    {
        val history = game().createPointsHistory()

        assertEquals(3, history.size)
        assertTrue(history.all { it == listOf(0) })
    }

    @Test
    fun `each round appends one running total per seat`()
    {
        val scores = listOf(
            wonBy(forehand, SkatSuit.CLUBS, 1),      // 24 -> forehand
            wonBy(middlehand, SkatSuit.GRAND, 1),    // 48 -> middlehand
            wonBy(forehand, SkatSuit.HEARTS, 1),     // 20 -> forehand
        )

        val history = game(scores).createPointsHistory()

        assertTrue(history.all { it.size == scores.size + 1 })
        assertEquals(listOf(0, 24, 24, 44), history[0])
        assertEquals(listOf(0, 0, 48, 48), history[1])
        assertEquals(listOf(0, 0, 0, 0), history[2])
    }

    @Test
    fun `a passed round keeps every series flat`()
    {
        val history = game(listOf(SkatScore.Passe.create())).createPointsHistory()

        assertTrue(history.all { it == listOf(0, 0) })
    }

    // The chart centres its vertical range on the data and never lets it collapse; a zero
    // height would divide by zero when mapping points to pixels.
    private fun verticalSpanOf(history: List<List<Int>>, minSpan: Float = 40f): Float
    {
        val all = history.flatten()
        val lowest = all.minOrNull() ?: 0
        val highest = all.maxOrNull() ?: 0
        return max((highest - lowest) / 2f, minSpan / 2f) * 2f
    }

    @Test
    fun `vertical range stays positive when every total is identical`()
    {
        val flat = game(listOf(SkatScore.Passe.create())).createPointsHistory()

        assertTrue(verticalSpanOf(flat) >= 40f)
    }

    @Test
    fun `vertical range grows with the spread of the totals`()
    {
        val scores = List(4) { wonBy(forehand, SkatSuit.GRAND, 3) } // 4 x 96
        val history = game(scores).createPointsHistory()

        assertEquals(384, history[0].last())
        assertEquals(384f, verticalSpanOf(history), 0.01f)
    }
}
