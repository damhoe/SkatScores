package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores

import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfScoresRepository
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.persistence.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoppelkopfScoreRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper,
    private val scoreDao: DoppelkopfScorePersistenceAdapter,
    private val databaseChanges: DatabaseChanges,
) : DoppelkopfScoresRepository
{
    /*
     * Every suspending call here hops to IO. The adapter below is blocking, and these are
     * called from viewModelScope, whose dispatcher is the main thread - so without this,
     * writing a round was database work on the thread drawing the frame.
     */

    override suspend fun insert(
        score: DoppelkopfScore,
        gameId: UUID,
        round: Int,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        scoreDao.insert(DoppelkopfScoreDto.mapFrom(score, gameId, round))
            .onSuccess { databaseChanges.notifyChanged() }
    }

    /**
     * Replaces a round with an edited version. The stored round number is kept, so the
     * position of the round in the list does not move.
     */
    override suspend fun update(score: DoppelkopfScore, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            val round = scoreDao.getRound(score.id).getOrNull()
                ?: return@withContext Result.failure(
                    NoSuchElementException("Unknown score ${score.id}")
                )

            scoreDao.update(DoppelkopfScoreDto.mapFrom(score, gameId, round))
                .onSuccess { databaseChanges.notifyChanged() }
        }

    /**
     * Removes a round and closes the gap it leaves in the round numbering.
     *
     * Both go in one transaction: rounds are dense and (game_id, round) is unique, so a
     * delete whose shift did not follow leaves a hole that the next round played would try
     * to reuse - and the insert would be refused from then on.
     */
    override suspend fun delete(id: UUID, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            val round = scoreDao.getRound(id).getOrNull()
                ?: return@withContext Result.failure(
                    NoSuchElementException("Unknown score $id")
                )

            dbHelper
                .transaction {
                    scoreDao.delete(id).getOrThrow()
                    scoreDao.shiftRoundsDown(gameId, round).getOrThrow()
                }
                .onSuccess { databaseChanges.notifyChanged() }
        }
}
