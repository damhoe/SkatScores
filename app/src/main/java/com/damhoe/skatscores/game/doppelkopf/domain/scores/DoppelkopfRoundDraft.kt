package com.damhoe.skatscores.game.doppelkopf.domain.scores

import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import java.util.UUID

enum class DoppelkopfRoundKind
{
    NORMAL,
    SOLO,
}

const val RE_SEAT_COUNT = 2

/** Editable form of any [DoppelkopfScore]; converts both ways without losing information. */
data class DoppelkopfRoundDraft(
    val scoreId: UUID? = null,
    val kind: DoppelkopfRoundKind = DoppelkopfRoundKind.NORMAL,

    /** Normal rounds only. */
    val reSeats: Set<UUID> = emptySet(),

    /** Solos only. */
    val soloist: Participant? = null,
    val soloKind: DoppelkopfSoloKind = DoppelkopfSoloKind.SUIT,

    val winner: DoppelkopfParty = RE,
    val winLevel: DoppelkopfWinLevel = DoppelkopfWinLevel.WON,
    val absage: DoppelkopfWinLevel? = null,
    val reAnnounced: Boolean = false,
    val kontraAnnounced: Boolean = false,
    val extraPointsRe: Int = 0,
    val extraPointsKontra: Int = 0,
)
{
    val isSolo: Boolean get() = kind == DoppelkopfRoundKind.SOLO

    val isEditingExistingRound: Boolean get() = scoreId != null

    val isComplete: Boolean
        get() = if (isSolo) soloist != null else reSeats.size == RE_SEAT_COUNT

    /** A solo has no extras (DDV). */
    val value: DoppelkopfRoundValue
        get() = DoppelkopfRoundValue(
            winLevel = winLevel,
            absage = absage,
            reAnnounced = reAnnounced,
            kontraAnnounced = kontraAnnounced,
            extraPointsRe = if (isSolo) 0 else extraPointsRe,
            extraPointsKontra = if (isSolo) 0 else extraPointsKontra,
        )

    /** Per member of the winning party. */
    fun points(countsExtraPoints: Boolean): Int
    {
        val base = value.basePoints(winner, isSolo)
        return if (countsExtraPoints) base + value.extraPoints(winner) else base
    }

    /** Null while undecided. */
    fun partyOf(participantId: UUID): DoppelkopfParty? = when
    {
        isSolo -> soloist?.let { if (it.id == participantId) RE else DoppelkopfParty.KONTRA }
        reSeats.isEmpty() -> null
        participantId in reSeats -> RE
        reSeats.size == RE_SEAT_COUNT -> DoppelkopfParty.KONTRA
        else -> null
    }

    fun toggleReSeat(participantId: UUID): DoppelkopfRoundDraft = when
    {
        participantId in reSeats -> copy(reSeats = reSeats - participantId)
        reSeats.size < RE_SEAT_COUNT -> copy(reSeats = reSeats + participantId)
        // When full, the oldest Re seat makes way for the new one.
        else -> copy(reSeats = reSeats.drop(1).toSet() + participantId)
    }

    fun toScore(id: UUID = scoreId ?: UUID.randomUUID()): DoppelkopfScore
    {
        if (isSolo)
        {
            val soloist = requireNotNull(soloist) { "A solo needs the seat that played alone" }
            return DoppelkopfScore.Solo(
                id = id,
                soloist = soloist.id,
                soloKind = soloKind,
                winner = winner,
                value = value,
            )
        }

        require(reSeats.size == RE_SEAT_COUNT) {
            "A normal round needs $RE_SEAT_COUNT seats on Re, but got ${reSeats.size}"
        }

        return DoppelkopfScore.Normal(
            id = id,
            reSeats = reSeats,
            winner = winner,
            value = value,
        )
    }

    companion object
    {
        fun forNewRound() = DoppelkopfRoundDraft()

        fun fromScore(
            score: DoppelkopfScore,
            participants: DoppelkopfParticipants,
        ): DoppelkopfRoundDraft
        {
            val value = score.value
            val common = DoppelkopfRoundDraft(
                scoreId = score.id,
                winner = score.winner,
                winLevel = value.winLevel,
                absage = value.absage,
                reAnnounced = value.reAnnounced,
                kontraAnnounced = value.kontraAnnounced,
                extraPointsRe = value.extraPointsRe,
                extraPointsKontra = value.extraPointsKontra,
            )

            return when (score)
            {
                is DoppelkopfScore.Normal -> common.copy(
                    kind = DoppelkopfRoundKind.NORMAL,
                    reSeats = score.reSeats,
                )

                is DoppelkopfScore.Solo -> common.copy(
                    kind = DoppelkopfRoundKind.SOLO,
                    soloist = participants.find(score.soloist),
                    soloKind = score.soloKind,
                )
            }
        }
    }
}
