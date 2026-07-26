package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import com.damhoe.skatscores.game.skat.domain.scores.SkatScore

sealed class AddOrUpdateScoreResult
{
    data class Added(val score: SkatScore) : AddOrUpdateScoreResult()
    data class Updated(val score: SkatScore) : AddOrUpdateScoreResult()
}