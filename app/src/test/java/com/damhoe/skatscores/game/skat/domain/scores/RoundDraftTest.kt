package com.damhoe.skatscores.game.skat.domain.scores

import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.common.WonOrLost.WON
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.Spitzen
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.GrandOrSuit.GrandOrSuitOptions
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.Null.NullOptions
import com.damhoe.skatscores.player.domain.PlayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RoundDraftTest
{
    private val forehand = Participant.Guest(UUID.randomUUID(), PlayerName("Marlon"))
    private val middlehand = Participant.Guest(UUID.randomUUID(), PlayerName("Evi"))
    private val rearhand = Participant.Guest(UUID.randomUUID(), PlayerName("Daniel"))
    private val participants = SkatParticipants(forehand, middlehand, rearhand)

    @Test
    fun `a draft without a declarer is a passed round worth nothing`()
    {
        val draft = RoundDraft(declarer = null)

        assertTrue(draft.isPasse)
        assertEquals(0, draft.points)
        assertTrue(draft.toSkatScore() is SkatScore.Passe)
    }

    @Test
    fun `points preview matches the score the draft produces`()
    {
        // Clubs, mit 2 -> 12 * 3 = 36
        val draft = RoundDraft(
            declarer = middlehand,
            game = RoundGame.CLUBS,
            spitzen = Spitzen(2),
            result = RoundResult.WON,
        )

        assertEquals(36, draft.points)
        assertEquals(draft.toSkatScore().toPoints(), draft.points)
    }

    @Test
    fun `hand and schwarz collapse onto the combined option`()
    {
        val draft = RoundDraft(
            declarer = forehand,
            game = RoundGame.GRAND,
            spitzen = Spitzen(2),
            hand = true,
            schneider = true,
            schwarz = true,
        )

        val score = draft.toSkatScore() as SkatScore.GrandOrSuit

        assertEquals(GrandOrSuitOptions.HAND_SCHWARZ, score.options)
        // Grand 24 * (2 + 1 + 3) = 144
        assertEquals(144, draft.points)
    }

    @Test
    fun `announcing schneider selects the announced option`()
    {
        val draft = RoundDraft(
            declarer = forehand,
            game = RoundGame.SPADES,
            schneider = true,
            announced = true,
        )

        val score = draft.toSkatScore() as SkatScore.GrandOrSuit

        assertEquals(GrandOrSuitOptions.SCHNEIDER_ANNOUNCED, score.options)
    }

    @Test
    fun `ouvert wins over the other win levels`()
    {
        val draft = RoundDraft(
            declarer = forehand,
            game = RoundGame.CLUBS,
            hand = true,
            schwarz = true,
            ouvert = true,
        )

        val score = draft.toSkatScore() as SkatScore.GrandOrSuit

        assertEquals(GrandOrSuitOptions.OUVERT, score.options)
    }

    @Test
    fun `a null game ignores spitzen and uses its own options`()
    {
        val draft = RoundDraft(
            declarer = rearhand,
            game = RoundGame.NULL,
            spitzen = Spitzen(4),
            hand = true,
            ouvert = true,
            result = RoundResult.WON,
        )

        val score = draft.toSkatScore() as SkatScore.Null

        assertFalse(draft.usesMultipliers)
        assertEquals(NullOptions.HAND_OUVERT, score.options)
        assertEquals(56, draft.points)
    }

    @Test
    fun `an overbid draft produces an overbid score and cannot be a null game`()
    {
        val draft = RoundDraft(
            declarer = forehand,
            game = RoundGame.CLUBS,
            result = RoundResult.OVERBID,
            bid = SkatBid(48),
        )

        val score = draft.toSkatScore() as SkatScore.Overbid

        assertTrue(draft.canBeOverbid)
        assertEquals(SkatBid(48), score.bid)
        assertFalse(RoundDraft(game = RoundGame.NULL).canBeOverbid)
    }

    @Test
    fun `a lost game is worth twice the negative game value`()
    {
        val won = RoundDraft(declarer = forehand, game = RoundGame.HEARTS, spitzen = Spitzen(1))
        val lost = won.copy(result = RoundResult.LOST)

        assertEquals(20, won.points)
        assertEquals(-40, lost.points)
    }

    // ---- Round trips: reopening an existing round must not lose anything ----

    private fun assertRoundTrips(score: SkatScore)
    {
        val draft = RoundDraft.fromScore(score, participants)
        val rebuilt = draft.toSkatScore()

        assertEquals(score.id, draft.scoreId)
        assertEquals(score.id, rebuilt.id)
        assertEquals(score.declarerId, rebuilt.declarerId)
        assertEquals(score.toPoints(), rebuilt.toPoints())
    }

    @Test
    fun `every grand or suit option round trips through a draft`()
    {
        GrandOrSuitOptions.entries
            // The declarer keeps this combination, but it has no multiplier of its own.
            .filterNot { it == GrandOrSuitOptions.SCHWARZ_ANNOUNCED_NOT_SCHNEIDER }
            .forEach { options ->
                assertRoundTrips(
                    SkatScore.GrandOrSuit(
                        id = UUID.randomUUID(),
                        skatParticipant = middlehand.id,
                        wonOrLost = WON,
                        suit = SkatSuit.SPADES,
                        spitzen = Spitzen(3),
                        options = options,
                    )
                )
            }
    }

    @Test
    fun `every null option round trips through a draft`()
    {
        (NullOptions.entries + null).forEach { options ->
            assertRoundTrips(
                SkatScore.Null(
                    id = UUID.randomUUID(),
                    skatParticipant = rearhand.id,
                    wonOrLost = LOST,
                    options = options,
                )
            )
        }
    }

    @Test
    fun `overbid and passe round trip through a draft`()
    {
        assertRoundTrips(
            SkatScore.Overbid(UUID.randomUUID(), forehand.id, SkatSuit.GRAND, SkatBid(72))
        )
        assertRoundTrips(SkatScore.Passe.create())
    }

    @Test
    fun `reopening a round keeps its identity so saving edits in place`()
    {
        val original = SkatScore.GrandOrSuit.create(
            soloPlayer = forehand,
            wonOrLost = WON,
            suit = SkatSuit.CLUBS,
            spitzen = Spitzen(1),
        )

        val edited = RoundDraft.fromScore(original, participants)
            .copy(result = RoundResult.LOST)

        assertTrue(edited.isEditingExistingRound)
        assertEquals(original.id, edited.toSkatScore().id)
        assertEquals(-48, edited.points)
    }

    @Test
    fun `a new round has no identity until it is saved`()
    {
        val draft = RoundDraft.forNewRound(forehand)

        assertFalse(draft.isEditingExistingRound)
        assertNull(draft.scoreId)

        val first = draft.toSkatScore()
        val second = draft.toSkatScore()

        assertFalse("each save must mint a fresh id", first.id == second.id)
    }
}
