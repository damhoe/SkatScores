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
    ): Result<Unit> = scoreDao.insert(SkatScoreDto.mapFrom(score, gameId, round))

    /**
     * Replaces a round with an edited version. The stored round number is kept, so the
     * position of the round in the list does not move.
     */
    override suspend fun update(score: SkatScore, gameId: UUID): Result<Unit>
    {
        val round = scoreDao.getRound(score.id).getOrNull()
            ?: return Result.failure(NoSuchElementException("Unknown score ${score.id}"))

        return scoreDao.update(SkatScoreDto.mapFrom(score, gameId, round))
    }

    /** Removes a round and closes the gap it leaves in the round numbering. */
    override suspend fun delete(id: UUID, gameId: UUID): Result<Unit>
    {
        val round = scoreDao.getRound(id).getOrNull()
            ?: return Result.failure(NoSuchElementException("Unknown score $id"))

        return scoreDao.delete(id)
            .mapCatching { scoreDao.shiftRoundsDown(gameId, round).getOrThrow() }
    }
}
