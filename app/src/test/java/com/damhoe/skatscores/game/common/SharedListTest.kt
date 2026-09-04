package com.damhoe.skatscores.game.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The shape of a shared list. The text is what leaves the app, so this pins down what a
 * reader gets: standings before rounds, places that survive a tie, and no empty section left
 * behind by a list that has nothing to say there yet.
 */
class SharedListTest
{
    @Test
    fun `a played list shares its standings and then its rounds`()
    {
        val text = sharedList(
            rounds = listOf(
                SharedRound(1, "Clubs · Hand", "Marlon · Won · 3×", "+60"),
                SharedRound(2, "Passe"),
            )
        ).asShareText()

        assertEquals(
            """
            Stammtisch 010
            Sa, 21. Feb · Round 2 / 24

            1. Marlon +60
            2. Evi 0
            3. Timo -60

            1. Clubs · Hand · Marlon · Won · 3× · +60
            2. Passe

            Scored with Skat Scores
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun `seats on the same total share a place`()
    {
        val text = sharedList(
            standings = listOf(
                SharedStanding("Marlon", 40),
                SharedStanding("Evi", 40),
                SharedStanding("Timo", -80),
            ),
        ).asShareText()

        // Second place is skipped, the way a scoreboard skips it: two seats hold the first.
        assertTrue(text.contains("1. Marlon +40"))
        assertTrue(text.contains("1. Evi +40"))
        assertTrue(text.contains("3. Timo -80"))
    }

    @Test
    fun `a list without rounds shares only its table`()
    {
        val text = sharedList().asShareText()

        assertEquals(
            """
            Stammtisch 010
            Sa, 21. Feb · Round 2 / 24

            1. Marlon +60
            2. Evi 0
            3. Timo -60

            Scored with Skat Scores
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun `a round with nothing to add to its name stays one line`()
    {
        val line = sharedList(rounds = listOf(SharedRound(7, "Passe")))
            .asShareText()
            .lines()
            .last { it.startsWith("7.") }

        assertEquals("7. Passe", line)
    }

    private fun sharedList(
        standings: List<SharedStanding> = listOf(
            SharedStanding("Marlon", 60),
            SharedStanding("Evi", 0),
            SharedStanding("Timo", -60),
        ),
        rounds: List<SharedRound> = emptyList(),
    ) = SharedList(
        title = "Stammtisch 010",
        subtitle = "Sa, 21. Feb · Round 2 / 24",
        standings = standings,
        rounds = rounds,
        footer = "Scored with Skat Scores",
    )
}
