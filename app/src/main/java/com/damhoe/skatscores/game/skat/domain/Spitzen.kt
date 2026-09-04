package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class Spitzen(val value: Int) :
    Parcelable
{
    init
    {
        require(value in 1..11) {
            "Invalid Spitzen value: $value"
        }
    }
}