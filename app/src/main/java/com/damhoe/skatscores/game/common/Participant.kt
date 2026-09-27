package com.damhoe.skatscores.game.common

import android.os.Parcelable
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
sealed interface Participant :
    Parcelable
{
    val id: UUID
    val displayName: String
        get() = name.value
    val name: PlayerName

    fun withId(id: UUID): Participant = when (this)
    {
        is Registered -> copy(id = id)
        is Guest -> copy(id = id)
    }

    @Parcelize
    data class Registered(
        override val id: UUID,
        override val name: PlayerName,
        val playerId: UUID,
    ) : Participant, Parcelable
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
    ) : Participant, Parcelable
    {
        constructor(name: PlayerName) : this(UUID.randomUUID(), name)

        override fun toString(): String = displayName
    }

    companion object
    {
        /** A name matching a registered player (ignoring case) links to that profile. */
        fun resolve(name: PlayerName, registeredPlayers: List<Player>): Participant
        {
            val player = registeredPlayers
                .firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }

            return if (player != null) Registered.from(player) else Guest(name)
        }
    }
}