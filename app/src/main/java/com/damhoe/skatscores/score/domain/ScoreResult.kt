package com.damhoe.skatscores.score.domain

sealed interface ScoreResult {
    data class Create(val score: SkatScore) : ScoreResult
    data class Update(val score: SkatScore) : ScoreResult
}