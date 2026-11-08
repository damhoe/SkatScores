package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Multiples of four, so every seat deals equally often. */
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
