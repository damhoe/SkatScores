package com.damhoe.skatscores.game.domain.skat

import android.os.Parcelable
import arrow.core.Either
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
class SkatSettings(
    val id: UUID,
    val playerCount : SkatPlayerCount,
    val firstDealer : SkatFirstDealer,
    val roundCount: SkatRoundCount,
    val scoringMode: SkatScoringMode) : Parcelable