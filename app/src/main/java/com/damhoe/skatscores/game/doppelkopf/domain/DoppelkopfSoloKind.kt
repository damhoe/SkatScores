package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Informational only: every solo scores the same. */
@Parcelize
enum class DoppelkopfSoloKind : Parcelable
{
    /** Farbsolo. */
    SUIT,

    /** Damensolo. */
    QUEENS,

    /** Bubensolo. */
    JACKS,

    /** Assensolo. */
    ACES,

    /** Fleischloser. */
    FLESHLESS,
}
