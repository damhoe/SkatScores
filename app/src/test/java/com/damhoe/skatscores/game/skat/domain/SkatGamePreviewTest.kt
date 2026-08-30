package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.common.WonOrLost.WON
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import com.damhoe.skatscores.player.domain.PlayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

class SkatGamePreviewTest
{
    private val forehand = Participant.Guest(UUID.randomUUID(), PlayerName("Marlon"))
    private val middlehand = Participant.Guest(UUID.randomUUID(), PlayerName("Evi"))
    private val rearhand = Participant.Guest(UUID.randomUUID(), PlayerName("Daniel"))

    private fun game(
        rounds: Int = 9,
        scores: List<SkatScore> = emptyList(),
    ) = SkatGame(
        id = UUID.randomUUID(),
        title = Title("Stammtisch 010"),
        playedAt = Instant.EPOCH,
        settings = SkatSettings(SkatRoundCount(rounds), SkatScoringMode.CLASSIC),
        participants = SkatParticipants(forehand, middlehand, rearhand),
        scores = scores,
    )

    private fun wonBy(declarer: Participant, suit: SkatSuit, spitzen: Int) =
        SkatScore.GrandOrSuit.create(declarer, WON, suit, Spitzen(spitzen))

    @Test
    fun `a fresh list is running and has no leader`()
    {
        val preview = SkatGamePreview.mapFrom(game(rounds = 3))

        assertTrue(preview.isRunning)
        assertEquals(0, preview.roundsPlayed)
        assertEquals(3, preview.totalRounds)
        assertEquals(0f, preview.progress, 0.001f)
        assertNull(preview.leaderPosition)
        assertNull(preview.leaderTotal)
    }

    @Test
    fun `a list is finished once the agreed rounds are played`()
    {
        val scores = List(3) { wonBy(forehand, SkatSuit.CLUBS, 1) }

        val preview = SkatGamePreview.mapFrom(game(rounds = 3, scores = scores))

        assertFalse(preview.isRunning)
        assertEquals(1f, preview.progress, 0.001f)
    }

    @Test
    fun `progress and totals follow the played rounds`()
    {
        // Clubs with 1 spitze -> 12 * 2 = 24 for forehand, six times.
        val scores = List(6) { wonBy(forehand, SkatSuit.CLUBS, 1) }

        val preview = SkatGamePreview.mapFrom(game(rounds = 12, scores = scores))

        assertEquals(0.5f, preview.progress, 0.001f)
        assertEquals(listOf(144, 0, 0), preview.totals)
        assertEquals(0, preview.leaderPosition)
        assertEquals(144, preview.leaderTotal)
    }

    @Test
    fun `player names and totals are in table order`()
    {
        val scores = listOf(wonBy(middlehand, SkatSuit.GRAND, 1))

        val preview = SkatGamePreview.mapFrom(game(scores = scores))

        assertEquals(listOf("Marlon", "Evi", "Daniel"), preview.playerNames)
        assertEquals(listOf(0, 48, 0), preview.totals)
        assertEquals(1, preview.leaderPosition)
    }

    @Test
    fun `dealer rotates from the last seat and never goes out of range`()
    {
        // Forehand is left of the dealer, so before the first round the last seat deals.
        val dealers = (0..4).map { playedRounds ->
            SkatGamePreview
                .mapFrom(game(rounds = 9, scores = List(playedRounds) {
                    wonBy(forehand, SkatSuit.CLUBS, 1)
                }))
                .dealerPosition
        }

        assertEquals(listOf(2, 0, 1, 2, 0), dealers)
        assertEquals("Daniel", SkatGamePreview.mapFrom(game(rounds = 9)).dealerName)
    }
}
