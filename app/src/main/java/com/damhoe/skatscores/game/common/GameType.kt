package com.damhoe.skatscores.game.common

/** Each game has its own tables and screens; only player profiles are shared. */
enum class GameType
{
    SKAT,
    DOPPELKOPF,
    ;

    companion object
    {
        val Default = SKAT

        fun fromNameOrDefault(name: String?): GameType =
            entries.firstOrNull { it.name == name } ?: Default
    }
}
