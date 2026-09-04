package com.damhoe.skatscores.game.doppelkopf.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DoppelkopfSettings(
    val roundCount: DoppelkopfRoundCount,
    val scoringMode: DoppelkopfScoringMode,
) : Parcelable
{
    val countsExtraPoints: Boolean
        get() = scoringMode == DoppelkopfScoringMode.DETAILED
}
