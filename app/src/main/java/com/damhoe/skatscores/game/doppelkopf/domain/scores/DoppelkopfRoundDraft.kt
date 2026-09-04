package com.damhoe.skatscores.game.doppelkopf.domain.scores

import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import java.util.UUID

/** Whether the round was played by two pairs or by one seat against the other three. */
enum class DoppelkopfRoundKind
{
    NORMAL,
    SOLO,
}

/** How many seats play as Re in a normal game. */
const val RE_SEAT_COUNT = 2

/**
 * A Doppelkopf round while it is being entered or edited.
 *
 * This is the single editable representation behind the round sheet: it holds every field of
 * both round kinds, converts to the matching [DoppelkopfScore], and can be rebuilt from one so
 * an existing round can be reopened without losing information.
 */
data class DoppelkopfRoundDraft(
    /** Null while entering a new round. */
    val scoreId: UUID? = null,
    val kind: DoppelkopfRoundKind = DoppelkopfRoundKind.NORMAL,

    /** The seats that held the queens of clubs. Only meaningful for a normal round. */
    val reSeats: Set<UUID> = emptySet(),

    /** The seat playing alone. Only meaningful for a solo. */
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

    /**
     * A normal round needs both queens of clubs placed; a solo needs the seat that played
     * alone. Until then there is nothing to save.
     */
    val isComplete: Boolean
        get() = if (isSolo) soloist != null else reSeats.size == RE_SEAT_COUNT

    val value: DoppelkopfRoundValue
        get() = DoppelkopfRoundValue(
            winLevel = winLevel,
            absage = absage,
            reAnnounced = reAnnounced,
            kontraAnnounced = kontraAnnounced,
            extraPointsRe = extraPointsRe,
            extraPointsKontra = extraPointsKontra,
        )

    /**
     * What the round would be worth to one member of the winning party. Drives the live
     * preview in the sheet, so it goes through the same arithmetic the score board uses.
     */
    fun points(countsExtraPoints: Boolean): Int
    {
        val base = value.basePoints(winner, isSolo)
        return if (countsExtraPoints) base + value.extraPoints(winner) else base
    }

    /** Which side a seat is on as the draft stands, or null while that is still open. */
    fun partyOf(participantId: UUID): DoppelkopfParty? = when
    {
        isSolo -> soloist?.let { if (it.id == participantId) RE else DoppelkopfParty.KONTRA }
        reSeats.isEmpty() -> null
        participantId in reSeats -> RE
        // Until both queens are placed the remaining seats are undecided rather than Kontra.
        reSeats.size == RE_SEAT_COUNT -> DoppelkopfParty.KONTRA
        else -> null
    }

    /** Toggles a seat between Re and Kontra, keeping at most [RE_SEAT_COUNT] on Re. */
    fun toggleReSeat(participantId: UUID): DoppelkopfRoundDraft = when
    {
        participantId in reSeats -> copy(reSeats = reSeats - participantId)
        reSeats.size < RE_SEAT_COUNT -> copy(reSeats = reSeats + participantId)
        // The row is full: the newest tap wins and the oldest seat drops off, so the user is
        // never stuck having to unselect before selecting.
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
        /** A blank round; who is Re is the first thing the sheet asks for. */
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
