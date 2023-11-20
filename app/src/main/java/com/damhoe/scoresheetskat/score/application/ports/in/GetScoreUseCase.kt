package com.damhoe.scoresheetskat.score.application.ports.`in`

import com.damhoe.scoresheetskat.score.domain.SkatScore

interface GetScoreUseCase {
    fun getScores(gameId: Long): Result<List<SkatScore>>
    fun getScore(id: Long): Result<SkatScore>
}