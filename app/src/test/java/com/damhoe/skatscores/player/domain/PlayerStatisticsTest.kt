package com.damhoe.skatscores.player.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStatisticsTest
{
    private fun skat(
        listsPlayed: Int = 1,
        roundsPlayed: Int = 0,
        soloRoundsPlayed: Int = 0,
        soloRoundsWon: Int = 0,
        roundsWonAsOpponent: Int = 0,
    ) = SkatPlayerStatistics(
        listsPlayed = listsPlayed,
        roundsPlayed = roundsPlayed,
        soloRoundsPlayed = soloRoundsPlayed,
        soloRoundsWon = soloRoundsWon,
        roundsWonAsOpponent = roundsWonAsOpponent,
    )

    @Test
    fun `solo share is measured against every round played`()
    {
        val stats = skat(roundsPlayed = 48, soloRoundsPlayed = 16)

        assertEquals(1.0 / 3, stats.soloShare, 0.0001)
    }

    @Test
    fun `solo win rate is measured against the rounds this player declared`()
    {
        // 8 of 16 declared rounds won, out of 48 rounds played. The rate is 50%, not 8/48:
        // dividing by the total rounds would silently punish a player who rarely declares.
        val stats = skat(
            roundsPlayed = 48,
            soloRoundsPlayed = 16,
            soloRoundsWon = 8,
        )

        assertEquals(0.5, stats.soloWinRate, 0.0001)
    }

    @Test
    fun `defender win rate is measured against the rounds somebody else declared`()
    {
        val stats = skat(
            roundsPlayed = 48,
            soloRoundsPlayed = 16,
            roundsWonAsOpponent = 16,
        )

        assertEquals(32, stats.opponentRoundsPlayed)
        assertEquals(0.5, stats.defenderWinRate, 0.0001)
    }

    @Test
    fun `a player who never declared has a zero solo win rate, not a crash`()
    {
        val stats = skat(roundsPlayed = 20, soloRoundsPlayed = 0)

        assertEquals(0.0, stats.soloWinRate, 0.0001)
        assertEquals(0.0, stats.soloShare, 0.0001)
    }

    @Test
    fun `a player who always declared has a zero defender win rate, not a crash`()
    {
        val stats = skat(roundsPlayed = 12, soloRoundsPlayed = 12)

        assertEquals(0, stats.opponentRoundsPlayed)
        assertEquals(0.0, stats.defenderWinRate, 0.0001)
        assertEquals(1.0, stats.soloShare, 0.0001)
    }

    @Test
    fun `a player in a list with no rounds yet has nothing to report`()
    {
        // Guards are tied to each denominator, so a list count above zero cannot let a
        // zero round count through into a division.
        val stats = PlayerStatistics(skat = skat(listsPlayed = 3, roundsPlayed = 0))

        assertFalse(stats.hasRounds)
        assertEquals(0.0, stats.skat.soloShare, 0.0001)
        assertEquals(0.0, stats.skat.soloWinRate, 0.0001)
        assertEquals(0.0, stats.skat.defenderWinRate, 0.0001)
    }

    @Test
    fun `rates stay within zero and one`()
    {
        val stats = skat(
            roundsPlayed = 30,
            soloRoundsPlayed = 10,
            soloRoundsWon = 10,
            roundsWonAsOpponent = 20,
        )

        listOf(stats.soloShare, stats.soloWinRate, stats.defenderWinRate).forEach {
            assertTrue("$it should be a share", it in 0.0..1.0)
        }
        assertEquals(1.0, stats.soloWinRate, 0.0001)
        assertEquals(1.0, stats.defenderWinRate, 0.0001)
    }

    // --- Both games together ---------------------------------------------------------------

    @Test
    fun `counts span both games, because profiles are shared`()
    {
        val stats = PlayerStatistics(
            skat = skat(listsPlayed = 3, roundsPlayed = 27),
            doppelkopf = DoppelkopfPlayerStatistics(listsPlayed = 2, roundsPlayed = 32),
        )

        assertEquals(5, stats.totalListsPlayed)
        assertEquals(59, stats.totalRoundsPlayed)
        assertTrue(stats.hasRounds)
    }

    @Test
    fun `a player with only Doppelkopf rounds still has something to report`()
    {
        // The screen hides a game with no rounds, so a Doppelkopf-only player must not read
        // as having played nothing at all.
        val stats = PlayerStatistics(
            doppelkopf = DoppelkopfPlayerStatistics(listsPlayed = 1, roundsPlayed = 16),
        )

        assertTrue(stats.hasRounds)
        assertFalse(stats.skat.hasRounds)
        assertTrue(stats.doppelkopf.hasRounds)
    }

    @Test
    fun `Doppelkopf rates are each measured against their own denominator`()
    {
        val stats = DoppelkopfPlayerStatistics(
            listsPlayed = 2,
            roundsPlayed = 32,
            roundsWon = 20,
            soloRoundsPlayed = 4,
            soloRoundsWon = 3,
            reRoundsPlayed = 16,
            reRoundsWon = 12,
        )

        assertEquals(20.0 / 32, stats.winRate, 0.0001)
        assertEquals(4.0 / 32, stats.soloShare, 0.0001)
        // Solo win rate is out of the solos played, not out of every round.
        assertEquals(3.0 / 4, stats.soloWinRate, 0.0001)
        assertEquals(12.0 / 16, stats.reWinRate, 0.0001)
    }

    @Test
    fun `a Doppelkopf player who never went alone has a zero solo win rate, not a crash`()
    {
        val stats = DoppelkopfPlayerStatistics(roundsPlayed = 16, soloRoundsPlayed = 0)

        assertEquals(0.0, stats.soloWinRate, 0.0001)
        assertEquals(0.0, stats.soloShare, 0.0001)
    }
}
