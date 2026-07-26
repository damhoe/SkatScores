package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import com.damhoe.skatscores.player.domain.PlayerName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SkatParticipants(
    val foreHand: SkatParticipant,
    val middleHand: SkatParticipant,
    val rearHand: SkatParticipant
) : Parcelable
{
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