package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
sealed class SkatPlayers(
    open val forehand: UUID?,
    open val middlehand: UUID?,
    open val rearhand: UUID?,
    val count: SkatPlayerCount
) : Parcelable
{
    class ThreePlayers(
        override val forehand: UUID?,
        override val middlehand: UUID?,
        override val rearhand: UUID?
    ) : SkatPlayers(
        forehand, middlehand, rearhand, SkatPlayerCount.THREE_PLAYERS
    )
    {
        constructor() : this(null, null, null)

        init
        {
            val corePlayers = listOfNotNull(forehand, middlehand, rearhand)

            require(corePlayers.size == corePlayers.toSet().size) {
                "Forehand, Middlehand, and Rearhand players must all be distinct."
            }
        }

        override fun toList(): List<UUID>
        {
            return listOfNotNull(forehand, middlehand, rearhand)
        }
    }

    class FourPlayers(
        override val forehand: UUID?,
        override val middlehand: UUID?,
        override val rearhand: UUID?,
        val dealer: UUID?,
    ) : SkatPlayers(
        forehand, middlehand, rearhand, SkatPlayerCount.FOUR_PLAYERS
    )
    {
        override fun toList(): List<UUID>
        {
            return listOfNotNull(forehand, middlehand, rearhand, dealer)
        }

        init
        {
            val allPlayers = listOfNotNull(forehand, middlehand, rearhand, dealer)

            require(allPlayers.size == allPlayers.toSet().size) {
                "All four players (forehand, middlehand, rearhand, and dealer) must be distinct."
            }
        }
    }

    abstract fun toList(): List<UUID>
}