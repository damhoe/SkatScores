package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** In a solo the soloist alone is Re. */
@Parcelize
enum class DoppelkopfParty : Parcelable
{
    RE,
    KONTRA,
    ;

    val other: DoppelkopfParty
        get() = if (this == RE) KONTRA else RE
}
