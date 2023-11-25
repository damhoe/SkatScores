package com.damhoe.skatscores.score.application.ports.`in`

import com.damhoe.skatscores.score.domain.SkatScore
import com.damhoe.skatscores.score.domain.SkatScoreCommand

interface CreateScoreUseCase {
    fun createScore(command: SkatScoreCommand): Result<SkatScore>
    fun updateScore(id: Long, command: SkatScoreCommand): Result<SkatScore>
    fun deleteScore(id: Long): Result<SkatScore>
    fun deleteScoresForGame(gameId: Long): Result<Int>
}