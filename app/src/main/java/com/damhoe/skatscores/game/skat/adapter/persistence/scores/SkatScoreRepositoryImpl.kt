package com.damhoe.skatscores.game.skat.adapter.persistence.scores

import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.persistence.DataTopic
import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.RoundSequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkatScoreRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper,
    private val scoreDao: SkatScorePersistenceAdapter,
    private val roundSequence: RoundSequence,
    private val databaseChanges: DatabaseChanges,
) : SkatScoresRepository
{
    /** The position is decided inside the write, from what the database holds then. */
    override suspend fun insert(score: SkatScore, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            dbHelper
                .transaction { db ->
                    val seq = roundSequence.next(db, after = scoreDao.lastSeq(gameId).getOrThrow())
                    scoreDao.insert(SkatScoreDto.mapFrom(score, gameId, seq)).getOrThrow()
                }
                .onSuccess { databaseChanges.notifyChanged(DataTopic.SKAT) }
        }

    override suspend fun update(score: SkatScore, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            scoreDao.update(SkatScoreDto.mapFrom(score, gameId, SEQ_IGNORED_ON_UPDATE))
                .onSuccess { databaseChanges.notifyChanged(DataTopic.SKAT) }
        }

    override suspend fun deleteLast(gameId: UUID): Result<UUID?> =
        withContext(Dispatchers.IO) {
            dbHelper
                .transaction { scoreDao.softDeleteLast(gameId).getOrThrow() }
                .onSuccess { removed ->
                    if (removed != null) databaseChanges.notifyChanged(DataTopic.SKAT)
                }
        }

    private companion object
    {
        const val SEQ_IGNORED_ON_UPDATE = ""
    }
}
