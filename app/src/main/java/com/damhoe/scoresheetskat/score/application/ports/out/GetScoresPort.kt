package com.damhoe.scoresheetskat.score.application.ports.out

import com.damhoe.scoresheetskat.score.domain.SkatScore

interface GetScoresPort {
    fun getScores(gameId: Long): Result<List<SkatScore>>
    fun getScore(id: Long): Result<SkatScore>
}