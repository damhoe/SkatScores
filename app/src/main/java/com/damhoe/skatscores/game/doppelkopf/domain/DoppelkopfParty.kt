package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The two sides of a Doppelkopf round.
 *
 * In a normal game Re is the pair holding the queens of clubs; in a solo the soloist is Re on
 * their own and the other three seats are Kontra.
 */
@Parcelize
enum class DoppelkopfParty : Parcelable
{
    RE,
    KONTRA,
    ;

    val other: DoppelkopfParty
        get() = if (this == RE) KONTRA else RE
}
