package com.damhoe.skatscores.game.domain.skat.application

import com.damhoe.skatscores.game.domain.skat.application.ports.CreateScoreUseCase
import com.damhoe.skatscores.game.domain.skat.application.ports.GetScoreUseCase
import com.damhoe.skatscores.game.domain.skat.application.ports.CreateScorePort
import com.damhoe.skatscores.game.domain.skat.application.ports.GetScoresPort
import com.damhoe.skatscores.game.domain.score.SkatScore
import com.damhoe.skatscores.game.domain.score.SkatScoreCommand
import javax.inject.Inject

class ScoreService @Inject constructor(
    private val getScoresPort: GetScoresPort,
    private val createScorePort: CreateScorePort)
    : CreateScoreUseCase, GetScoreUseCase
{
    override fun createScore(command: SkatScoreCommand): Result<SkatScore> =
        SkatScore(command).let { createScorePort.saveScore(it) }

    override fun getSkatGameScores(gameId: Long): Result<List<SkatScore>> =
        getScoresPort.getScores(gameId)

    override fun getScore(id: Long): Result<SkatScore> = getScoresPort.getScore(id)

    override fun updateScore(id: Long, command: SkatScoreCommand): Result<SkatScore> =
        SkatScore(command)
            .apply {
                this.id = id
            }
            .let { score ->
                createScorePort.updateScore(score).map { score }
            }

    override fun deleteScore(id: Long): Result<SkatScore> = createScorePort.deleteScore(id)

    override fun deleteScoresForGame(gameId: Long): Result<Int> =
        createScorePort.deleteScoresForGame(gameId)
}