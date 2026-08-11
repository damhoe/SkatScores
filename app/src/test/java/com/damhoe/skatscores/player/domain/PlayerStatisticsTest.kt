package com.damhoe.skatscores.player.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStatisticsTest
{
    private fun statistics(
        totalGamesPlayed: Int = 1,
        totalRoundsPlayed: Int = 0,
        soloRoundsPlayed: Int = 0,
        soloRoundsWon: Int = 0,
        roundsWonAsOpponent: Int = 0,
    ) = PlayerStatistics(
        totalGamesPlayed = totalGamesPlayed,
        totalRoundsPlayed = totalRoundsPlayed,
        soloRoundsPlayed = soloRoundsPlayed,
        soloRoundsWon = soloRoundsWon,
        roundsWonAsOpponent = roundsWonAsOpponent,
    )

    @Test
    fun `solo share is measured against every round played`()
    {
        val stats = statistics(totalRoundsPlayed = 48, soloRoundsPlayed = 16)

        assertEquals(1.0 / 3, stats.soloShare, 0.0001)
    }

    @Test
    fun `solo win rate is measured against the rounds this player declared`()
    {
        // 8 of 16 declared rounds won, out of 48 rounds played. The rate is 50%, not 8/48:
        // dividing by the total rounds would silently punish a player who rarely declares.
        val stats = statistics(
            totalRoundsPlayed = 48,
            soloRoundsPlayed = 16,
            soloRoundsWon = 8,
        )

        assertEquals(0.5, stats.soloWinRate, 0.0001)
    }

    @Test
    fun `defender win rate is measured against the rounds somebody else declared`()
    {
        val stats = statistics(
            totalRoundsPlayed = 48,
            soloRoundsPlayed = 16,
            roundsWonAsOpponent = 16,
        )

        assertEquals(32, stats.opponentRoundsPlayed)
        assertEquals(0.5, stats.defenderWinRate, 0.0001)
    }

    @Test
    fun `a player who never declared has a zero solo win rate, not a crash`()
    {
        val stats = statistics(totalRoundsPlayed = 20, soloRoundsPlayed = 0)

        assertEquals(0.0, stats.soloWinRate, 0.0001)
        assertEquals(0.0, stats.soloShare, 0.0001)
    }

    @Test
    fun `a player who always declared has a zero defender win rate, not a crash`()
    {
        val stats = statistics(totalRoundsPlayed = 12, soloRoundsPlayed = 12)

        assertEquals(0, stats.opponentRoundsPlayed)
        assertEquals(0.0, stats.defenderWinRate, 0.0001)
        assertEquals(1.0, stats.soloShare, 0.0001)
    }

    @Test
    fun `a player in a list with no rounds yet has nothing to report`()
    {
        // Guards are tied to each denominator, so a game count above zero cannot let a
        // zero round count through into a division.
        val stats = statistics(totalGamesPlayed = 3, totalRoundsPlayed = 0)

        assertFalse(stats.hasRounds)
        assertEquals(0.0, stats.soloShare, 0.0001)
        assertEquals(0.0, stats.soloWinRate, 0.0001)
        assertEquals(0.0, stats.defenderWinRate, 0.0001)
    }

    @Test
    fun `rates stay within zero and one`()
    {
        val stats = statistics(
            totalRoundsPlayed = 30,
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
}
