package com.damhoe.skatscores.score.application

import com.damhoe.skatscores.score.application.ports.`in`.CreateScoreUseCase
import com.damhoe.skatscores.score.application.ports.`in`.GetScoreUseCase
import com.damhoe.skatscores.score.application.ports.out.CreateScorePort
import com.damhoe.skatscores.score.application.ports.out.GetScoresPort
import com.damhoe.skatscores.score.domain.SkatScore
import com.damhoe.skatscores.score.domain.SkatScoreCommand
import javax.inject.Inject

class ScoreService @Inject constructor(
    private val getScoresPort: GetScoresPort,
    private val createScorePort: CreateScorePort
) : CreateScoreUseCase, GetScoreUseCase {

    override fun createScore(command: SkatScoreCommand): Result<SkatScore> =
        SkatScore(command).let { createScorePort.saveScore(it) }

    override fun getScores(gameId: Long): Result<List<SkatScore>> = getScoresPort.getScores(gameId)

    override fun getScore(id: Long): Result<SkatScore> = getScoresPort.getScore(id)

    override fun updateScore(id: Long, command: SkatScoreCommand): Result<SkatScore> =
        SkatScore(command).let { score ->
            score.id = id
            createScorePort.updateScore(score).map { score }
        }

    override fun deleteScore(id: Long): Result<SkatScore> = createScorePort.deleteScore(id)

    override fun deleteScoresForGame(gameId: Long): Result<Int> =
        createScorePort.deleteScoresForGame(gameId)
}