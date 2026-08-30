package com.damhoe.skatscores.game.doppelkopf.domain.scores

import android.os.Parcelable
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.KONTRA
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import kotlinx.parcelize.Parcelize

/** An announcement is worth this much to whichever side ends up winning. */
private const val ANNOUNCEMENT_POINTS = 2

/** A normal game won by Kontra is worth one point more than the same game won by Re. */
private const val AGAINST_THE_ELDERS_POINTS = 1

/**
 * What a Doppelkopf round is worth, before it is spread over the seats.
 *
 * Everything here is counted for the winning party: the win itself and the levels below it,
 * the announcements made by either side, the levels announced in advance, and - when the list
 * counts them - the extras collected during play.
 */
@Parcelize
data class DoppelkopfRoundValue(
    val winLevel: DoppelkopfWinLevel = DoppelkopfWinLevel.WON,

    /** Level announced in advance ("keine 90" and below), or null when nothing was announced. */
    val absage: DoppelkopfWinLevel? = null,

    val reAnnounced: Boolean = false,
    val kontraAnnounced: Boolean = false,

    /** Foxes caught, doppelkopf tricks and Karlchen, counted for Re. */
    val extraPointsRe: Int = 0,

    /** The same for Kontra. */
    val extraPointsKontra: Int = 0,
) : Parcelable
{
    /**
     * Win, levels and announcements. This is what every list counts, whatever its scoring
     * mode, and it is the value a round reports as its own points.
     */
    fun basePoints(winner: DoppelkopfParty, isSolo: Boolean): Int
    {
        var points = winLevel.points

        // "Gegen die Alten": beating the pair that held the queens of clubs is worth more.
        // A solo has no elders to play against, so it does not apply there.
        if (!isSolo && winner == KONTRA) points += AGAINST_THE_ELDERS_POINTS

        if (reAnnounced) points += ANNOUNCEMENT_POINTS
        if (kontraAnnounced) points += ANNOUNCEMENT_POINTS

        absage?.let { points += it.absagePoints }

        return points
    }

    /**
     * The extras, netted in favour of [winner]: each side's own extras count for it, so a
     * fox caught by the losing party comes off the winner's score.
     */
    fun extraPoints(winner: DoppelkopfParty): Int = when (winner)
    {
        RE -> extraPointsRe - extraPointsKontra
        KONTRA -> extraPointsKontra - extraPointsRe
    }

    val hasExtraPoints: Boolean
        get() = extraPointsRe > 0 || extraPointsKontra > 0
}
