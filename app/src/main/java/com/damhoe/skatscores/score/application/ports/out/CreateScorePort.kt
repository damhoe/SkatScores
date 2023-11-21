package com.damhoe.skatscores.score.application.ports.out

import com.damhoe.skatscores.score.domain.SkatScore

interface CreateScorePort {
    fun saveScore(score: SkatScore): Result<SkatScore>
    fun updateScore(score: SkatScore): Result<Unit>
    fun deleteScore(id: Long): Result<SkatScore>
    fun deleteScoresForGame(gameId: Long): Result<Int>
}