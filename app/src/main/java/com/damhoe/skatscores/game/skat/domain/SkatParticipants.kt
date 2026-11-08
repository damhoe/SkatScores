package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.common.Participant
import android.os.Parcelable
import com.damhoe.skatscores.player.domain.PlayerName
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class SkatParticipants(
    val foreHand: Participant,
    val middleHand: Participant,
    val rearHand: Participant
) : Parcelable
{
    /** Table order, which is also the score board's column order. */
    fun asList(): List<Participant> = listOf(foreHand, middleHand, rearHand)

    fun seatOf(participantId: UUID): Int? =
        asList().indexOfFirst { it.id == participantId }.takeIf { it >= 0 }

    /** Same people and seats with new ids; participant ids belong to one list. */
    fun asNewParticipants(): SkatParticipants = SkatParticipants(
        foreHand = foreHand.withId(UUID.randomUUID()),
        middleHand = middleHand.withId(UUID.randomUUID()),
        rearHand = rearHand.withId(UUID.randomUUID()),
    )

    companion object
    {
        /** Placeholder guests until the players are picked. */
        fun createNew(): SkatParticipants
        {
            return SkatParticipants(
                foreHand = Participant.Guest(PlayerName("Player 1")),
                middleHand = Participant.Guest(PlayerName("Player 2")),
                rearHand = Participant.Guest(PlayerName("Player 3"))
            )
        }
    }
}