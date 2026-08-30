package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores

import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfScoresRepository
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import com.damhoe.skatscores.persistence.DatabaseChanges
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoppelkopfScoreRepositoryImpl @Inject constructor(
    private val scoreDao: DoppelkopfScorePersistenceAdapter,
    private val databaseChanges: DatabaseChanges,
) : DoppelkopfScoresRepository
{
    override suspend fun insert(
        score: DoppelkopfScore,
        gameId: UUID,
        round: Int,
    ): Result<Unit> = scoreDao.insert(DoppelkopfScoreDto.mapFrom(score, gameId, round))
        .onSuccess { databaseChanges.notifyChanged() }

    /**
     * Replaces a round with an edited version. The stored round number is kept, so the
     * position of the round in the list does not move.
     */
    override suspend fun update(score: DoppelkopfScore, gameId: UUID): Result<Unit>
    {
        val round = scoreDao.getRound(score.id).getOrNull()
            ?: return Result.failure(NoSuchElementException("Unknown score ${score.id}"))

        return scoreDao.update(DoppelkopfScoreDto.mapFrom(score, gameId, round))
            .onSuccess { databaseChanges.notifyChanged() }
    }

    /** Removes a round and closes the gap it leaves in the round numbering. */
    override suspend fun delete(id: UUID, gameId: UUID): Result<Unit>
    {
        val round = scoreDao.getRound(id).getOrNull()
            ?: return Result.failure(NoSuchElementException("Unknown score $id"))

        return scoreDao.delete(id)
            .mapCatching { scoreDao.shiftRoundsDown(gameId, round).getOrThrow() }
            .onSuccess { databaseChanges.notifyChanged() }
    }
}
