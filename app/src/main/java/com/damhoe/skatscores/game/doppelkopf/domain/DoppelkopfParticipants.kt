package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.player.domain.PlayerName
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * The four people at a Doppelkopf table, in seat order.
 *
 * Doppelkopf is played by exactly four, so unlike Skat the seats have no names of their own -
 * who is Re and who is Kontra is decided per round, not by where somebody sits.
 */
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

    /** The participants in table order, i.e. the column order used on the score board. */
    fun asList(): List<Participant> = seats

    /** Column of the given participant on the score board, or null if not at this table. */
    fun seatOf(participantId: UUID): Int? =
        seats.indexOfFirst { it.id == participantId }.takeIf { it >= 0 }

    fun find(participantId: UUID): Participant? = seats.firstOrNull { it.id == participantId }

    /**
     * The same people at the same seats, but as fresh participants. Used when starting a new
     * list from an old one: participant ids are per-list, so they must not be shared.
     */
    fun asNewParticipants(): DoppelkopfParticipants =
        DoppelkopfParticipants(seats.map { it.withId(UUID.randomUUID()) })

    companion object
    {
        const val SEAT_COUNT = 4

        /**
         * Placeholder seats for a list that has just been created. These are visible until
         * the players are picked in the game view, so they read as slots to fill.
         */
        fun createNew(): DoppelkopfParticipants = DoppelkopfParticipants(
            List(SEAT_COUNT) { seat -> Participant.Guest(PlayerName("Player ${seat + 1}")) }
        )
    }
}
