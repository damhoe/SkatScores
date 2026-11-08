package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class DoppelkopfScoringMode : Parcelable
{
    /** Win, levels and announcements. */
    SIMPLE,

    /** Also foxes, Doppelkopf tricks and Karlchen. */
    DETAILED,
}
