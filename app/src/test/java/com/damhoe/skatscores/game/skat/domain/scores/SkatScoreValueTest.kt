package com.damhoe.skatscores.game.skat.domain.scores

import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.common.WonOrLost.WON
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.Null.NullOptions
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

/** Game values against the ISkO tables. */
class SkatScoreValueTest
{
    private val declarer = UUID.randomUUID()

    private fun overbid(suit: SkatSuit, bid: Int) =
        SkatScore.Overbid(UUID.randomUUID(), declarer, suit, SkatBid(bid)).toPoints()

    private fun nullGame(options: NullOptions?, result: com.damhoe.skatscores.game.common.WonOrLost) =
        SkatScore.Null(UUID.randomUUID(), declarer, result, options).toPoints()

    @Test
    fun `an overbid game is the lowest multiple reaching the bid, lost and so doubled`()
    {
        // Diamonds: 9 -> 18 reaches 18
        assertEquals(-36, overbid(SkatSuit.DIAMONDS, 18))
        // Clubs: 12 -> 24 -> 36 -> 48 reaches 40
        assertEquals(-96, overbid(SkatSuit.CLUBS, 40))
        // Grand: 24 -> 48 -> 72 reaches 72
        assertEquals(-144, overbid(SkatSuit.GRAND, 72))
    }

    @Test
    fun `null games are worth 23, 35, 46 and 59`()
    {
        assertEquals(23, nullGame(null, WON))
        assertEquals(35, nullGame(NullOptions.HAND, WON))
        assertEquals(46, nullGame(NullOptions.OUVERT, WON))
        assertEquals(59, nullGame(NullOptions.HAND_OUVERT, WON))
    }

    @Test
    fun `a lost null game counts double`()
    {
        assertEquals(-92, nullGame(NullOptions.OUVERT, LOST))
        assertEquals(-118, nullGame(NullOptions.HAND_OUVERT, LOST))
    }
}
