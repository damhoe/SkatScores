package com.damhoe.skatscores.game.score.application.ports.out

import com.damhoe.skatscores.game.score.domain.SkatScore

interface GetScoresPort {
    fun getScores(gameId: Long): Result<List<SkatScore>>
    fun getScore(id: Long): Result<SkatScore>
}