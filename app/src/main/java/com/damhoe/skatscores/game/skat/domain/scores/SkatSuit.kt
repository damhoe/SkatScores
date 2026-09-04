package com.damhoe.skatscores.game.skat.domain.scores

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SkatSuit
    : Parcelable
{
    GRAND,
    CLUBS,
    SPADES,
    HEARTS,
    DIAMONDS,
}