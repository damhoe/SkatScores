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

    companion object
    {
        fun createNew(): SkatParticipants
        {
            return SkatParticipants(
                foreHand = SkatParticipant.Guest(PlayerName("Player F")),
                middleHand = SkatParticipant.Guest(PlayerName("Player M")),
                rearHand = SkatParticipant.Guest(PlayerName("Player R"))
            )
        }
    }
}