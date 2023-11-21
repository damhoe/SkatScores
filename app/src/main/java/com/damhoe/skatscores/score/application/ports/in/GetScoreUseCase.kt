package com.damhoe.skatscores.score.application.ports.`in`

import com.damhoe.skatscores.score.domain.SkatScore

interface GetScoreUseCase {
    fun getScores(gameId: Long): Result<List<SkatScore>>
    fun getScore(id: Long): Result<SkatScore>
}