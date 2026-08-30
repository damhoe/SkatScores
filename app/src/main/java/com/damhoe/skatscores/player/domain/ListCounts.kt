package com.damhoe.skatscores.player.domain

/**
 * How many lists a player appears in, split by game.
 *
 * Profiles are shared between Skat and Doppelkopf, so a bare total ("5 lists") hides which
 * table those lists were played at. The two counts are kept apart all the way to the screen,
 * which names the games rather than adding them up.
 */
data class ListCounts(
    val skat: Int = 0,
    val doppelkopf: Int = 0,
)
{
    val total: Int
        get() = skat + doppelkopf

    val isEmpty: Boolean
        get() = total == 0
}
