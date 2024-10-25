package com.damhoe.skatscores.game.domain.skat.application.ports

import com.damhoe.skatscores.game.domain.score.SkatScore
import com.damhoe.skatscores.game.domain.score.SkatScoreCommand

interface CreateScoreUseCase {
    fun createScore(command: SkatScoreCommand): Result<SkatScore>
    fun updateScore(id: Long, command: SkatScoreCommand): Result<SkatScore>
    fun deleteScore(id: Long): Result<SkatScore>
    fun deleteScoresForGame(gameId: Long): Result<Int>
}