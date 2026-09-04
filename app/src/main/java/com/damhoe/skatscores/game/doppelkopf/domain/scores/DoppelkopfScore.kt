package com.damhoe.skatscores.game.doppelkopf.domain.scores

import android.os.Parcelable
import com.damhoe.skatscores.game.common.Score
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.KONTRA
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import kotlinx.parcelize.Parcelize
import java.util.UUID

/** A soloist plays for three, so their side of the round counts three times. */
const val SOLO_FACTOR = 3

/**
 * One played Doppelkopf round.
 *
 * A round is always won by one of the two parties, so unlike a Skat round there is no passed
 * case: [winner] and [value] are what every round has, and the two subclasses only differ in
 * how the seats are divided between the parties.
 */
@Parcelize
sealed class DoppelkopfScore(
    override val id: UUID,
) : Score(id), Parcelable
{
    abstract val winner: DoppelkopfParty
    abstract val value: DoppelkopfRoundValue

    /** Which side a seat played on, or null when the seat was not part of this round. */
    abstract fun partyOf(participantId: UUID): DoppelkopfParty?

    abstract val isSolo: Boolean

    /** The seat that played alone, or null for a normal game. */
    abstract val soloistId: UUID?

    /**
     * What the round is worth to one member of the winning party, before the extras a
     * detailed list adds on top. Mirrors Skat, where a score also reports its plain game
     * value and the scoring mode is applied when the totals are added up.
     */
    override fun toPoints(): Int = value.basePoints(winner, isSolo)

    /**
     * Points this round is worth for one seat, given what the round came to. The seats of a
     * round always add up to zero: a soloist wins or loses three times what each opponent
     * does, and in a normal game the two winners take what the two losers give up.
     */
    fun pointsFor(participantId: UUID, roundValue: Int): Int
    {
        val party = partyOf(participantId) ?: return 0
        val sign = if (party == winner) 1 else -1
        val factor = if (participantId == soloistId) SOLO_FACTOR else 1

        return sign * factor * roundValue
    }

    /** A normal game: two seats hold the queens of clubs and play as Re. */
    class Normal(
        override val id: UUID,
        /** The two seats that played as Re. */
        val reSeats: Set<UUID>,
        override val winner: DoppelkopfParty,
        override val value: DoppelkopfRoundValue,
    ) : DoppelkopfScore(id)
    {
        override val isSolo: Boolean get() = false
        override val soloistId: UUID? get() = null

        override fun partyOf(participantId: UUID): DoppelkopfParty =
            if (participantId in reSeats) RE else KONTRA

        companion object
        {
            fun create(
                reSeats: Set<UUID>,
                winner: DoppelkopfParty,
                value: DoppelkopfRoundValue,
            ) = Normal(UUID.randomUUID(), reSeats, winner, value)
        }
    }

    /** A solo: one seat is Re on their own against the other three. */
    class Solo(
        override val id: UUID,
        val soloist: UUID,
        val soloKind: DoppelkopfSoloKind,
        override val winner: DoppelkopfParty,
        override val value: DoppelkopfRoundValue,
    ) : DoppelkopfScore(id)
    {
        override val isSolo: Boolean get() = true
        override val soloistId: UUID get() = soloist

        override fun partyOf(participantId: UUID): DoppelkopfParty =
            if (participantId == soloist) RE else KONTRA

        /** Whether the soloist came out on top; the round log reads better that way. */
        val soloistWon: Boolean get() = winner == RE

        companion object
        {
            fun create(
                soloist: UUID,
                soloKind: DoppelkopfSoloKind,
                winner: DoppelkopfParty,
                value: DoppelkopfRoundValue,
            ) = Solo(UUID.randomUUID(), soloist, soloKind, winner, value)
        }
    }
}
