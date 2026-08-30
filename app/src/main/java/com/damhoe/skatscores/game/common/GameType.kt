package com.damhoe.skatscores.game.common

/**
 * The card game a list is scored with.
 *
 * Lists never mix: a list belongs to one game from the moment it is created, and each game
 * keeps its own tables, setup, settings and round entry. Player profiles are the one thing
 * both share.
 */
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
