package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * How many rounds a Doppelkopf list runs for.
 *
 * Only multiples of four are offered, so that the deal comes back round to every seat the
 * same number of times.
 */
@JvmInline
@Parcelize
value class DoppelkopfRoundCount(val value: Int) : Parcelable
{
    init
    {
        require(value in ALLOWED_VALUES) {
            "value must be one of ${ALLOWED_VALUES.joinToString()}, but was $value"
        }
    }

    companion object
    {
        val ALLOWED_VALUES = listOf(4, 8, 12, 16, 20, 24, 32)
    }
}
