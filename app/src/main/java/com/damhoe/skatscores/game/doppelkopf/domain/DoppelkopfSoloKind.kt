package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Which solo was played.
 *
 * Every solo is worth the same - the soloist plays for three - so this is recorded for the
 * round log rather than for the scoring.
 */
@Parcelize
enum class DoppelkopfSoloKind : Parcelable
{
    /** Farbsolo: one suit is trump instead of diamonds. */
    SUIT,

    /** Damensolo: only the queens are trump. */
    QUEENS,

    /** Bubensolo: only the jacks are trump. */
    JACKS,

    /** Assensolo. */
    ACES,

    /** Fleischloser: no trumps at all. */
    FLESHLESS,
}
