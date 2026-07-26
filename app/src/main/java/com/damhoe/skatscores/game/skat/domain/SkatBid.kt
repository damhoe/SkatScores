package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class SkatBid(val value: Int) : Parcelable
{
    companion object
    {
        val BiddingValues: Set<Int> = setOf(
            18, 20, 22, 23, 24, 27, 30, 33, 35, 36, 40, 44, 45, 46, 48, 50,
            54, 55, 59, 60, 63, 66, 70, 72, 77, 81, 84, 88, 90, 96, 99, 108, 120
        )
    }

    init
    {
        require(value in BiddingValues) {
            "Skat bid must be one of the standard bidding ladder values. Got $value"
        }
    }
}