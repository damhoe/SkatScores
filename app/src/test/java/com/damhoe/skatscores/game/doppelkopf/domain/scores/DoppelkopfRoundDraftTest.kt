package com.damhoe.skatscores.game.doppelkopf.domain.scores

import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.KONTRA
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import com.damhoe.skatscores.player.domain.PlayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.util.UUID
import org.junit.Test

class DoppelkopfRoundDraftTest
{
    private val seats = List(4) { seat ->
        Participant.Guest(UUID.randomUUID(), PlayerName("P${seat + 1}"))
    }
    private val participants = DoppelkopfParticipants(seats)

    @Test
    fun `a new round has nothing to save until the queens are placed`()
    {
        var draft = DoppelkopfRoundDraft.forNewRound()
        assertFalse(draft.isComplete)

        draft = draft.toggleReSeat(seats[0].id)
        assertFalse(draft.isComplete)

        draft = draft.toggleReSeat(seats[1].id)
        assertTrue(draft.isComplete)
    }

    @Test
    fun `tapping a seat again takes it off Re`()
    {
        val draft = DoppelkopfRoundDraft.forNewRound()
            .toggleReSeat(seats[0].id)
            .toggleReSeat(seats[0].id)

        assertEquals(emptySet<UUID>(), draft.reSeats)
    }

    @Test
    fun `a third tap replaces the oldest seat rather than being ignored`()
    {
        // Otherwise a user who picked the wrong pair would have to unselect before selecting,
        // which reads as a dead chip.
        val draft = DoppelkopfRoundDraft.forNewRound()
            .toggleReSeat(seats[0].id)
            .toggleReSeat(seats[1].id)
            .toggleReSeat(seats[2].id)

        assertEquals(setOf(seats[1].id, seats[2].id), draft.reSeats)
    }

    @Test
    fun `a solo is complete as soon as the soloist is picked`()
    {
        val draft = DoppelkopfRoundDraft.forNewRound()
            .copy(kind = DoppelkopfRoundKind.SOLO, soloist = seats[2])

        assertTrue(draft.isComplete)
        assertEquals(RE, draft.partyOf(seats[2].id))
        assertEquals(KONTRA, draft.partyOf(seats[0].id))
    }

    @Test
    fun `seats stay undecided until both queens are placed`()
    {
        val draft = DoppelkopfRoundDraft.forNewRound().toggleReSeat(seats[0].id)

        assertEquals(RE, draft.partyOf(seats[0].id))
        // One queen placed says nothing about where the other one is.
        assertEquals(null, draft.partyOf(seats[3].id))
    }

    @Test
    fun `the preview follows whether the list counts extras`()
    {
        val draft = DoppelkopfRoundDraft.forNewRound()
            .toggleReSeat(seats[0].id)
            .toggleReSeat(seats[1].id)
            .copy(extraPointsRe = 2)

        assertEquals(1, draft.points(countsExtraPoints = false))
        assertEquals(3, draft.points(countsExtraPoints = true))
    }

    @Test
    fun `a normal round survives a round trip through its score`()
    {
        val draft = DoppelkopfRoundDraft.forNewRound()
            .toggleReSeat(seats[0].id)
            .toggleReSeat(seats[2].id)
            .copy(
                winner = KONTRA,
                winLevel = DoppelkopfWinLevel.NO_60,
                absage = DoppelkopfWinLevel.NO_90,
                kontraAnnounced = true,
                extraPointsKontra = 1,
            )

        val restored = DoppelkopfRoundDraft.fromScore(draft.toScore(), participants)

        assertEquals(DoppelkopfRoundKind.NORMAL, restored.kind)
        assertEquals(setOf(seats[0].id, seats[2].id), restored.reSeats)
        assertEquals(KONTRA, restored.winner)
        assertEquals(DoppelkopfWinLevel.NO_60, restored.winLevel)
        assertEquals(DoppelkopfWinLevel.NO_90, restored.absage)
        assertTrue(restored.kontraAnnounced)
        assertEquals(1, restored.extraPointsKontra)
    }

    @Test
    fun `a solo survives a round trip through its score`()
    {
        val draft = DoppelkopfRoundDraft.forNewRound().copy(
            kind = DoppelkopfRoundKind.SOLO,
            soloist = seats[3],
            soloKind = DoppelkopfSoloKind.JACKS,
            winner = RE,
            reAnnounced = true,
        )

        val restored = DoppelkopfRoundDraft.fromScore(draft.toScore(), participants)

        assertEquals(DoppelkopfRoundKind.SOLO, restored.kind)
        assertEquals(seats[3], restored.soloist)
        assertEquals(DoppelkopfSoloKind.JACKS, restored.soloKind)
        assertTrue(restored.reAnnounced)
    }

    @Test
    fun `an edited round keeps its identity`()
    {
        val draft = DoppelkopfRoundDraft.forNewRound()
            .toggleReSeat(seats[0].id)
            .toggleReSeat(seats[1].id)
        val score = draft.toScore()

        val reopened = DoppelkopfRoundDraft.fromScore(score, participants)

        assertTrue(reopened.isEditingExistingRound)
        assertEquals(score.id, reopened.toScore().id)
    }
}
