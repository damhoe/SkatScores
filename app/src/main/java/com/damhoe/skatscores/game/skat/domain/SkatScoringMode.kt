package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SkatScoringMode : Parcelable
{
    CLASSIC,
    TOURNAMENT,
}