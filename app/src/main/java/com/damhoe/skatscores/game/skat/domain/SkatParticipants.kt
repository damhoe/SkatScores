package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import com.damhoe.skatscores.player.domain.PlayerName
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class SkatParticipants(
    val foreHand: SkatParticipant,
    val middleHand: SkatParticipant,
    val rearHand: SkatParticipant
) : Parcelable
{
    /** The participants in table order, i.e. the column order used on the score board. */
    fun asList(): List<SkatParticipant> = listOf(foreHand, middleHand, rearHand)

    /** Column of the given participant on the score board, or null if not at this table. */
    fun seatOf(participantId: UUID): Int? =
        asList().indexOfFirst { it.id == participantId }.takeIf { it >= 0 }

    /**
     * The same people at the same seats, but as fresh participants. Used when starting a new
     * list from an old one: participant ids are per-list, so they must not be shared.
     */
    fun asNewParticipants(): SkatParticipants = SkatParticipants(
        foreHand = foreHand.withId(UUID.randomUUID()),
        middleHand = middleHand.withId(UUID.randomUUID()),
        rearHand = rearHand.withId(UUID.randomUUID()),
    )

    companion object
    {
        /**
         * Placeholder seats for a list that has just been created. These are visible until
         * the players are picked in the game view, so they read as slots to fill rather than
         * as table positions.
         */
        fun createNew(): SkatParticipants
        {
            return SkatParticipants(
                foreHand = SkatParticipant.Guest(PlayerName("Player 1")),
                middleHand = SkatParticipant.Guest(PlayerName("Player 2")),
                rearHand = SkatParticipant.Guest(PlayerName("Player 3"))
            )
        }
    }
}