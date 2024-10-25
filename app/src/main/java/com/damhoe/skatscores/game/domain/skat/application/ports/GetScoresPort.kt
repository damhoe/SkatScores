package com.damhoe.skatscores.game.domain.skat.application.ports

import com.damhoe.skatscores.game.domain.score.SkatScore

interface GetScoresPort {
    fun getScores(gameId: Long): Result<List<SkatScore>>
    fun getScore(id: Long): Result<SkatScore>
}