package com.damhoe.skatscores.game.doppelkopf.domain.scores

import android.os.Parcelable
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.KONTRA
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty.RE
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import kotlinx.parcelize.Parcelize

/** Goes to the winning side, whoever announced. */
private const val ANNOUNCEMENT_POINTS = 2

/** "Gegen die Alten": Kontra winning a normal game. */
private const val AGAINST_THE_ELDERS_POINTS = 1

/** Everything is counted for the winning party. */
@Parcelize
data class DoppelkopfRoundValue(
    val winLevel: DoppelkopfWinLevel = DoppelkopfWinLevel.WON,

    /** "Keine 90" and below; null when nothing was announced. */
    val absage: DoppelkopfWinLevel? = null,

    val reAnnounced: Boolean = false,
    val kontraAnnounced: Boolean = false,

    /** Foxes, Doppelkopf tricks and Karlchen. */
    val extraPointsRe: Int = 0,

    val extraPointsKontra: Int = 0,
) : Parcelable
{
    /** Counted in every scoring mode. */
    fun basePoints(winner: DoppelkopfParty, isSolo: Boolean): Int
    {
        var points = winLevel.points

        if (!isSolo && winner == KONTRA) points += AGAINST_THE_ELDERS_POINTS

        if (reAnnounced) points += ANNOUNCEMENT_POINTS
        if (kontraAnnounced) points += ANNOUNCEMENT_POINTS

        absage?.let { points += it.absagePoints }

        return points
    }

    /** Net in favour of [winner]; the losing side's extras are subtracted. */
    fun extraPoints(winner: DoppelkopfParty): Int = when (winner)
    {
        RE -> extraPointsRe - extraPointsKontra
        KONTRA -> extraPointsKontra - extraPointsRe
    }

    val hasExtraPoints: Boolean
        get() = extraPointsRe > 0 || extraPointsKontra > 0
}
