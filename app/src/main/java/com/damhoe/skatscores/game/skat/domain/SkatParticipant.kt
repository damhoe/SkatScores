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

    /**
     * The same participant under an existing id. Used to keep a seat's identity stable when
     * the person sitting there is replaced, so that recorded scores keep their column.
     */
    fun withId(id: UUID): SkatParticipant = when (this)
    {
        is Registered -> copy(id = id)
        is Guest -> copy(id = id)
    }

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

    companion object
    {
        /**
         * Turns a typed name into a participant: a name that matches a registered player
         * links the seat to that profile, anything else becomes a guest.
         */
        fun resolve(name: PlayerName, registeredPlayers: List<Player>): SkatParticipant
        {
            val player = registeredPlayers
                .firstOrNull { it.name.value.equals(name.value, ignoreCase = true) }

            return if (player != null) Registered.from(player) else Guest(name)
        }
    }
}