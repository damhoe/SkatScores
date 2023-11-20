package com.damhoe.scoresheetskat.score.application.ports.out

import com.damhoe.scoresheetskat.score.domain.SkatScore

interface CreateScorePort {
    fun saveScore(score: SkatScore): Result<SkatScore>
    fun updateScore(score: SkatScore): Result<Unit>
    fun deleteScore(id: Long): Result<SkatScore>
    fun deleteScoresForGame(gameId: Long): Result<Int>
}