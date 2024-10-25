package com.damhoe.skatscores.player.domain

import java.time.OffsetDateTime

class Dummy(
    id: PlayerId,
    name: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime) :
    Player(
        id,
        name,
        createdAt,
        updatedAt)
{
    companion object
    {
        const val DUMMY_PATTERN: String = "^(Player|Spieler)(\\s\\d+|in\\s\\d+)$"
    }
}