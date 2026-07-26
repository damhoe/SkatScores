package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class SkatRoundCount(val value: Int) : Parcelable
{
    init
    {
        require(value in ALLOWED_VALUES) {
            "value must be one of 3, 9, 12, 18, 24, or 32, but was $value"
        }
    }

    companion object {
        val ALLOWED_VALUES = listOf(3, 9, 12, 18, 24, 32)
    }
}