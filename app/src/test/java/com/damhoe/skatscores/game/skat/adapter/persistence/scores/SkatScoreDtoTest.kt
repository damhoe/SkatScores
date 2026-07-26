package com.damhoe.skatscores.game.skat.adapter.persistence.scores

import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.Spitzen
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import com.damhoe.skatscores.player.domain.PlayerName
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class SkatScoreDtoTest
{
    private val gameId = UUID.randomUUID()
    private val declarer = SkatParticipant.Guest(UUID.randomUUID(), PlayerName("Declarer"))

    private fun roundTrip(score: SkatScore, round: Int = 0): SkatScore =
        SkatScoreDto.mapFrom(score, gameId, round).toSkatScore()

    @Test
    fun `overbid survives the round trip through the dto`()
    {
        val score = SkatScore.Overbid.create(declarer, SkatSuit.SPADES, SkatBid(48))

        val restored = roundTrip(score) as SkatScore.Overbid

        assertEquals(score.id, restored.id)
        assertEquals(declarer.id, restored.skatParticipant)
        assertEquals(SkatSuit.SPADES, restored.suit)
        assertEquals(SkatBid(48), restored.bid)
        assertEquals(score.toPoints(), restored.toPoints())
    }

    @Test
    fun `overbid is mapped to its own score type and keeps the declarer`()
    {
        val score = SkatScore.Overbid.create(declarer, SkatSuit.GRAND, SkatBid(72))

        val dto = SkatScoreDto.mapFrom(score, gameId, round = 3)

        assertEquals(SkatScoreType.OVERBID, dto.type)
        assertEquals(gameId, dto.gameId)
        assertEquals(3, dto.round)
        assertEquals(declarer.id, dto.skatParticipantId)
        assertEquals(SkatSuit.GRAND, dto.overbidSuit)
        assertEquals(72, dto.overbidBidValue)
    }

    @Test
    fun `passe survives the round trip and stays without a declarer`()
    {
        val score = SkatScore.Passe.create()

        val dto = SkatScoreDto.mapFrom(score, gameId, round = 1)
        val restored = roundTrip(score)

        assertEquals(null, dto.skatParticipantId)
        assertEquals(score.id, restored.id)
        assertEquals(null, restored.declarerId)
    }

    @Test
    fun `grand or suit survives the round trip through the dto`()
    {
        val score = SkatScore.GrandOrSuit.create(
            soloPlayer = declarer,
            wonOrLost = LOST,
            suit = SkatSuit.HEARTS,
            spitzen = Spitzen(2),
            options = SkatScore.GrandOrSuit.GrandOrSuitOptions.HAND,
        )

        val restored = roundTrip(score) as SkatScore.GrandOrSuit

        assertEquals(declarer.id, restored.skatParticipant)
        assertEquals(LOST, restored.wonOrLost)
        assertEquals(SkatSuit.HEARTS, restored.suit)
        assertEquals(Spitzen(2), restored.spitzen)
        assertEquals(SkatScore.GrandOrSuit.GrandOrSuitOptions.HAND, restored.options)
        assertEquals(score.toPoints(), restored.toPoints())
    }

    @Test
    fun `null game survives the round trip through the dto`()
    {
        val score = SkatScore.Null.create(
            soloPlayer = declarer,
            wonOrLost = LOST,
            options = SkatScore.Null.NullOptions.HAND_OUVERT,
        )

        val restored = roundTrip(score) as SkatScore.Null

        assertEquals(declarer.id, restored.skatParticipant)
        assertEquals(LOST, restored.wonOrLost)
        assertEquals(SkatScore.Null.NullOptions.HAND_OUVERT, restored.options)
        assertEquals(score.toPoints(), restored.toPoints())
    }
}
