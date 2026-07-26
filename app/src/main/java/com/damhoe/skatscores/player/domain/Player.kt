package com.damhoe.skatscores.player.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID

@Parcelize
data class Player(
    val id: UUID = UUID.randomUUID(),
    val name: PlayerName,
    val createdAt: Instant = Instant.now(),
) : Parcelable
{
    fun updateName(newName: PlayerName): Player
    {
        return Player(
            id, newName, createdAt
        )
    }
}
