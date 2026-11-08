package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Cumulative: [points] counts the win plus every level below it. */
@Parcelize
enum class DoppelkopfWinLevel(val points: Int) : Parcelable
{
    /** The other side took 90 or more. */
    WON(1),
    NO_90(2),
    NO_60(3),
    NO_30(4),

    /** The other side took no trick at all. */
    SCHWARZ(5),
    ;

    /** Added by announcing this level in advance ("keine 90", …); 0 for the plain win. */
    val absagePoints: Int
        get() = points - 1

    companion object
    {
        val Absagen: List<DoppelkopfWinLevel> = listOf(NO_90, NO_60, NO_30, SCHWARZ)
    }
}
