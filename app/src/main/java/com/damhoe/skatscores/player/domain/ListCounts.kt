package com.damhoe.skatscores.player.domain

/** Kept per game: the screens name the games rather than showing one total. */
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
