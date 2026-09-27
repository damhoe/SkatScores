package com.damhoe.skatscores.game.skat.domain.scores

import android.os.Parcelable
import com.damhoe.skatscores.game.common.Score
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.skat.domain.Spitzen
import kotlinx.parcelize.Parcelize
import java.util.UUID

private const val CLUBS_POINTS = 12
private const val SPADES_POINTS = 11
private const val HEARTS_POINTS = 10
private const val DIAMONDS_POINTS = 9
private const val GRAND_POINTS = 24

@Parcelize
sealed class SkatScore(
    override val id: UUID,
) : Score(id), Parcelable
{
    /** Null for a passed round. */
    abstract val declarerId: UUID?

    /** Null for a passed round. */
    abstract val result: WonOrLost?

    class Passe(override val id: UUID) : SkatScore(id)
    {
        companion object
        {
            fun create() = Passe(UUID.randomUUID())
        }

        override val declarerId: UUID? get() = null
        override val result: WonOrLost? get() = null

        override fun toPoints() = 0
    }

    class Overbid(
        override val id: UUID,
        val skatParticipant: UUID,
        val suit: SkatSuit,
        val bid: SkatBid,
    ) : SkatScore(id)
    {
        companion object
        {
            fun create(
                soloPlayer: Participant,
                suit: SkatSuit,
                bid: SkatBid,
            ) = Overbid(
                id = UUID.randomUUID(),
                skatParticipant = soloPlayer.id,
                suit = suit,
                bid = bid,
            )
        }

        override val declarerId: UUID? get() = skatParticipant

        override val result: WonOrLost? get() = LOST

        override fun toPoints(): Int
        {
            val suitValue = when (suit)
            {
                SkatSuit.CLUBS -> CLUBS_POINTS
                SkatSuit.HEARTS -> HEARTS_POINTS
                SkatSuit.DIAMONDS -> DIAMONDS_POINTS
                SkatSuit.SPADES -> SPADES_POINTS
                SkatSuit.GRAND -> GRAND_POINTS
            }

            // ISkO: the lowest multiple of the base value reaching the bid, lost and so doubled.
            var points = suitValue
            while (points < bid.value) points += suitValue

            return -2 * points
        }
    }

    class Null(
        override val id: UUID,
        val skatParticipant: UUID,
        val wonOrLost: WonOrLost,
        val options: NullOptions?,
    ) : SkatScore(id)
    {
        companion object
        {
            fun create(
                soloPlayer: Participant,
                wonOrLost: WonOrLost,
                options: NullOptions? = null,
            ) = Null(
                id = UUID.randomUUID(),
                skatParticipant = soloPlayer.id,
                wonOrLost = wonOrLost,
                options = options,
            )
        }

        enum class NullOptions
        {
            HAND,
            OUVERT,
            HAND_OUVERT,
        }

        override val declarerId: UUID? get() = skatParticipant
        override val result: WonOrLost? get() = wonOrLost

        override fun toPoints() = (options
            ?.let {
                when (it)
                {
                    NullOptions.HAND -> 35
                    NullOptions.OUVERT -> 46
                    NullOptions.HAND_OUVERT -> 59
                }
            } ?: 23)
            .let { if (wonOrLost == LOST) -2 * it else it }
    }

    class GrandOrSuit(
        override val id: UUID,
        val skatParticipant: UUID,
        val wonOrLost: WonOrLost,
        val suit: SkatSuit,
        val spitzen: Spitzen,
        val options: GrandOrSuitOptions?,
    ) : SkatScore(id)
    {
        companion object
        {
            fun create(
                soloPlayer: Participant,
                wonOrLost: WonOrLost,
                suit: SkatSuit,
                spitzen: Spitzen,
                options: GrandOrSuitOptions? = null,
            ) = GrandOrSuit(
                id = UUID.randomUUID(),
                skatParticipant = soloPlayer.id,
                wonOrLost = wonOrLost,
                suit = suit,
                spitzen = spitzen,
                options = options,
            )
        }

        enum class GrandOrSuitOptions
        {
            HAND,
            SCHNEIDER,
            SCHWARZ,
            HAND_SCHNEIDER,
            HAND_SCHWARZ,
            SCHNEIDER_ANNOUNCED,
            SCHWARZ_ANNOUNCED,
            SCHWARZ_ANNOUNCED_NOT_SCHNEIDER,
            OUVERT,
        }

        override val declarerId: UUID? get() = skatParticipant
        override val result: WonOrLost? get() = wonOrLost

        override fun toPoints(): Int
        {
            val suitValue = when (suit)
            {
                SkatSuit.CLUBS -> CLUBS_POINTS
                SkatSuit.HEARTS -> HEARTS_POINTS
                SkatSuit.DIAMONDS -> DIAMONDS_POINTS
                SkatSuit.SPADES -> SPADES_POINTS
                SkatSuit.GRAND -> GRAND_POINTS
            }

            var multiplier = spitzen.value + 1

            multiplier += when (options) {
                GrandOrSuitOptions.HAND -> 1
                GrandOrSuitOptions.SCHNEIDER -> 1
                GrandOrSuitOptions.SCHWARZ -> 2
                GrandOrSuitOptions.HAND_SCHNEIDER -> 2
                GrandOrSuitOptions.HAND_SCHWARZ -> 3
                GrandOrSuitOptions.SCHNEIDER_ANNOUNCED -> 3
                GrandOrSuitOptions.SCHWARZ_ANNOUNCED -> 5
                GrandOrSuitOptions.OUVERT -> 6
                else -> 0
            }

            val points = suitValue * multiplier
            return points
                .let { if (wonOrLost == LOST) -2 * it else it }
        }
    }
}
