package com.damhoe.skatscores.library

import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewListDraftTest
{
    @Test
    fun `every offered round count is one the domain accepts`()
    {
        SkatRoundCount.ALLOWED_VALUES.forEach { value ->
            assertEquals(value, SkatRoundCount(value).value)
        }
    }

    @Test
    fun `the handoff round counts are not all valid`()
    {
        val candidates = listOf(12, 24, 36, 48)
        val accepted = candidates.filter { it in SkatRoundCount.ALLOWED_VALUES }

        assertEquals(listOf(12, 24), accepted)
    }

    @Test
    fun `a default draft starts on a supported round count`()
    {
        val draft = NewListDraft()

        assertTrue(draft.roundCount.value in SkatRoundCount.ALLOWED_VALUES)
    }

    @Test
    fun `a new list seats placeholders, since players are picked in the game view`()
    {
        val seats = SkatParticipants.createNew().asList()

        assertEquals(3, seats.size)
        assertEquals(3, seats.map { it.displayName }.toSet().size)
        assertTrue(seats.all { it is Participant.Guest })
    }
}
