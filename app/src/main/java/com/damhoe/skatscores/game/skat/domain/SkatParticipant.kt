package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
sealed interface SkatParticipant :
    Parcelable
{
    val id: UUID
    val displayName: String
        get() = name.value
    val name: PlayerName

    @Parcelize
    data class Registered(
        override val id: UUID,
        override val name: PlayerName,
        val playerId: UUID,
    ) : SkatParticipant, Parcelable
    {
        override fun toString(): String = displayName

        companion object
        {
            fun from(player: Player): Registered
            {
                return Registered(UUID.randomUUID(), player.name, player.id)
            }
        }
    }

    @Parcelize
    data class Guest(
        override val id: UUID,
        override val name: PlayerName
    ) : SkatParticipant, Parcelable
    {
        constructor(name: PlayerName) : this(UUID.randomUUID(), name)

        override fun toString(): String = displayName
    }
}