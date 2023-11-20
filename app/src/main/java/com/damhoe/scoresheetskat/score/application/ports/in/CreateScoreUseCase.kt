package com.damhoe.scoresheetskat.score.application.ports.`in`

import com.damhoe.scoresheetskat.score.domain.SkatScore
import com.damhoe.scoresheetskat.score.domain.SkatScoreCommand

interface CreateScoreUseCase {
    fun createScore(command: SkatScoreCommand): Result<SkatScore>
    fun updateScore(id: Long, command: SkatScoreCommand): Result<Unit>
    fun deleteScore(id: Long): Result<SkatScore>
    fun deleteScoresForGame(gameId: Long): Result<Int>
}