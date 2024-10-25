package com.damhoe.skatscores.game.domain.skat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SkatScoringMode : Parcelable
{
    Standard,
    Tournament
}