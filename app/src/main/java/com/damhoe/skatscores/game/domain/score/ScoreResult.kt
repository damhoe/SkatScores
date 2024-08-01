package com.damhoe.skatscores.game.domain.score

sealed class ScoreResult {
    data class Create(val score: SkatScore) : ScoreResult()
    data class Update(val score: SkatScore) : ScoreResult()
}