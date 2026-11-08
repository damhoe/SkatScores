package com.damhoe.skatscores.game.doppelkopf.domain.scores

import android.os.Parcelable
import com.damhoe.skatscores.game.common.Score
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.KONTRA
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import kotlinx.parcelize.Parcelize
import java.util.UUID

const val SOLO_FACTOR = 3

@Parcelize
sealed class DoppelkopfScore(
    override val id: UUID,
) : Score(id), Parcelable
{
    abstract val winner: DoppelkopfParty
    abstract val value: DoppelkopfRoundValue

    /** Null when the seat is not in this round. */
    abstract fun partyOf(participantId: UUID): DoppelkopfParty?

    abstract val isSolo: Boolean

    abstract val soloistId: UUID?

    /** Per member of the winning party, without extras. */
    override fun toPoints(): Int = value.basePoints(winner, isSolo)

    /** Zero-sum over the seats: the soloist gets three times an opponent's share. */
    fun pointsFor(participantId: UUID, roundValue: Int): Int
    {
        val party = partyOf(participantId) ?: return 0
        val sign = if (party == winner) 1 else -1
        val factor = if (participantId == soloistId) SOLO_FACTOR else 1

        return sign * factor * roundValue
    }

    class Normal(
        override val id: UUID,
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
