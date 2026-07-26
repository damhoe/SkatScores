package com.damhoe.skatscores.game.skat.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class SkatSettings(
    val roundCount: SkatRoundCount,
    val scoringMode: SkatScoringMode
) : Parcelable

