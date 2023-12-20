package com.damhoe.skatscores.game.score.application.ports.`in`

import com.damhoe.skatscores.game.score.domain.SkatScore
import com.damhoe.skatscores.game.score.domain.SkatScoreCommand

interface CreateScoreUseCase {
    fun createScore(command: SkatScoreCommand): Result<SkatScore>
    fun updateScore(id: Long, command: SkatScoreCommand): Result<SkatScore>
    fun deleteScore(id: Long): Result<SkatScore>
    fun deleteScoresForGame(gameId: Long): Result<Int>
}