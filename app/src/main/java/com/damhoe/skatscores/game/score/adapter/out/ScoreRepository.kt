package com.damhoe.skatscores.game.score.adapter.out

import com.damhoe.skatscores.game.score.application.ports.out.CreateScorePort
import com.damhoe.skatscores.game.score.application.ports.out.GetScoresPort
import com.damhoe.skatscores.game.score.domain.SkatScore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreRepository @Inject constructor(private val persistenceAdapter: ScorePersistenceAdapter) :
    CreateScorePort, GetScoresPort {

    override fun saveScore(score: SkatScore): Result<SkatScore> =
        ScoreDto.fromScore(score).let { scoreDto ->
            persistenceAdapter.insertScore(scoreDto).mapCatching {
                score.apply { id = it }
            }
        }

    override fun updateScore(score: SkatScore): Result<Unit> =
        ScoreDto.fromScore(score).let { scoreDto ->
            persistenceAdapter.updateScore(scoreDto)
        }

    override fun deleteScore(id: Long): Result<SkatScore> =
        persistenceAdapter.deleteScore(id).mapCatching {
            it.toScore()
        }

    // Returns number of deleted games
    override fun deleteScoresForGame(gameId: Long): Result<Int> =
        persistenceAdapter.deleteScoresForGame(gameId)

    override fun getScores(gameId: Long): Result<List<SkatScore>> =
        persistenceAdapter.getScoresForGame(gameId).mapCatching { scoreDtoList ->
            scoreDtoList.map { it.toScore() }
        }

    override fun getScore(id: Long): Result<SkatScore> =
        persistenceAdapter.getScore(id).mapCatching { it.toScore() }
}