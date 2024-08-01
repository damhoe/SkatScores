package com.damhoe.skatscores.game.domain.score

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed class ScoreRequest : Parcelable {
    @Parcelize data class CreateScoreRequest(
        val gameId: Long,
        val names: List<String>,
        val positions: List<Int>
    ) : ScoreRequest()

    @Parcelize data class UpdateScoreRequest(
        val scoreId: Long,
        val names: List<String>,
        val positions: List<Int>
    ) : ScoreRequest()
}