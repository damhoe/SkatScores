package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * How much of the round is written down.
 *
 * Both modes count the win, the levels below 90 and the announcements; they differ in whether
 * the extras collected during play are counted as well. Turning them off is the usual house
 * rule at a table that does not want to keep track of foxes and doppelkopf tricks, and it is
 * the only thing about a Doppelkopf list that changes what a round is worth.
 */
@Parcelize
enum class DoppelkopfScoringMode : Parcelable
{
    /** Win, levels and announcements only. */
    SIMPLE,

    /** Also counts caught foxes, doppelkopf tricks and Karlchen. */
    DETAILED,
}
