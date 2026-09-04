package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * How clearly a Doppelkopf round was won, as the card points the losing party was held to.
 *
 * The levels are cumulative: keeping the other side under 60 also keeps it under 90, so
 * [points] counts the win itself plus every level reached below it.
 */
@Parcelize
enum class DoppelkopfWinLevel(val points: Int) : Parcelable
{
    /** Won with the other side on 90 or more. */
    WON(1),
    NO_90(2),
    NO_60(3),
    NO_30(4),

    /** The other side took no trick at all. */
    SCHWARZ(5),
    ;

    /**
     * What announcing this level in advance ("keine 90", "keine 60", …) adds on top of the
     * round. Winning is not something you announce, so the plain win is worth nothing here.
     */
    val absagePoints: Int
        get() = points - 1

    /** The levels that can be announced in advance, in the order the sheet offers them. */
    companion object
    {
        val Absagen: List<DoppelkopfWinLevel> = listOf(NO_90, NO_60, NO_30, SCHWARZ)
    }
}
