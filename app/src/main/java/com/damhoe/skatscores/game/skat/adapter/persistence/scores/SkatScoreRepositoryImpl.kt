package com.damhoe.skatscores.game.skat.adapter.persistence.scores

import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkatScoreRepositoryImpl @Inject constructor(
    private val scoreDao: SkatScorePersistenceAdapter
) : SkatScoresRepository
{
    override fun getScoresOfGame(gameId: UUID): Flow<SkatScore>
    {
        return scoreDao.getScoresForGame(gameId)
            .map { dto -> dto.toSkatScore() }
            .asFlow()
    }

    override suspend fun insert(
        score: SkatScore,
        gameId: UUID,
        round: Int,
    ): Result<Unit>
    {
        scoreDao.insert(SkatScoreDto.mapFrom(score, gameId, round))
        return Result.success(Unit)
    }

    override suspend fun update(score: SkatScore): Result<Unit>
    {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: UUID): Result<Unit>
    {
        TODO("Not yet implemented")
    }
}