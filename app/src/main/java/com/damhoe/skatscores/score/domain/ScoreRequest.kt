package com.damhoe.skatscores.score.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed class ScoreRequest

@Parcelize
data class CreateScoreRequest(
    val gameId: Long,
    val names: List<String>,
    val positions: List<Int>
) : ScoreRequest(), Parcelable

@Parcelize
data class UpdateScoreRequest(
    val scoreId: Long,
    val names: List<String>,
    val positions: List<Int>
) : ScoreRequest(), Parcelable