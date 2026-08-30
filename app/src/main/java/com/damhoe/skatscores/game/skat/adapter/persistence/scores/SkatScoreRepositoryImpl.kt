package com.damhoe.skatscores.game.skat.adapter.persistence.scores

import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.persistence.DatabaseChanges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkatScoreRepositoryImpl @Inject constructor(
    private val scoreDao: SkatScorePersistenceAdapter,
    private val databaseChanges: DatabaseChanges,
) : SkatScoresRepository
{
    /*
     * Every suspending call here hops to IO. The adapter below is blocking, and these are
     * called from viewModelScope, whose dispatcher is the main thread - so without this,
     * writing a round was database work on the thread drawing the frame.
     */

    override suspend fun insert(
        score: SkatScore,
        gameId: UUID,
        round: Int,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        scoreDao.insert(SkatScoreDto.mapFrom(score, gameId, round))
            .onSuccess { databaseChanges.notifyChanged() }
    }

    /**
     * Replaces a round with an edited version. The stored round number is kept, so the
     * position of the round in the list does not move.
     */
    override suspend fun update(score: SkatScore, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            val round = scoreDao.getRound(score.id).getOrNull()
                ?: return@withContext Result.failure(
                    NoSuchElementException("Unknown score ${score.id}")
                )

            scoreDao.update(SkatScoreDto.mapFrom(score, gameId, round))
                .onSuccess { databaseChanges.notifyChanged() }
        }

    /** Removes a round and closes the gap it leaves in the round numbering. */
    override suspend fun delete(id: UUID, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            val round = scoreDao.getRound(id).getOrNull()
                ?: return@withContext Result.failure(
                    NoSuchElementException("Unknown score $id")
                )

            scoreDao.delete(id)
                .mapCatching { scoreDao.shiftRoundsDown(gameId, round).getOrThrow() }
                .onSuccess { databaseChanges.notifyChanged() }
        }
}
