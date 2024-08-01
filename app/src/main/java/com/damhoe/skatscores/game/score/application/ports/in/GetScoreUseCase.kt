package com.damhoe.skatscores.game.score.application.ports.`in`

import com.damhoe.skatscores.game.domain.score.SkatScore

interface GetScoreUseCase {
    fun getScores(gameId: Long): Result<List<SkatScore>>
    fun getScore(id: Long): Result<SkatScore>
}