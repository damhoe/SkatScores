package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.player.domain.PlayerName
import kotlinx.parcelize.Parcelize
import java.util.UUID

/** Seats have no roles of their own: Re and Kontra are decided per round. */
@Parcelize
data class DoppelkopfParticipants(
    val seats: List<Participant>,
) : Parcelable
{
    init
    {
        require(seats.size == SEAT_COUNT) {
            "A Doppelkopf table has $SEAT_COUNT seats, but got ${seats.size}"
        }
    }

    /** Table order, which is also the score board's column order. */
    fun asList(): List<Participant> = seats

    fun seatOf(participantId: UUID): Int? =
        seats.indexOfFirst { it.id == participantId }.takeIf { it >= 0 }

    fun find(participantId: UUID): Participant? = seats.firstOrNull { it.id == participantId }

    /** Same people and seats with new ids; participant ids belong to one list. */
    fun asNewParticipants(): DoppelkopfParticipants =
        DoppelkopfParticipants(seats.map { it.withId(UUID.randomUUID()) })

    companion object
    {
        const val SEAT_COUNT = 4

        /** Placeholder guests until the players are picked. */
        fun createNew(): DoppelkopfParticipants = DoppelkopfParticipants(
            List(SEAT_COUNT) { seat -> Participant.Guest(PlayerName("Player ${seat + 1}")) }
        )
    }
}
