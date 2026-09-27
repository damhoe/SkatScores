package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores

import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfScoresRepository
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
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
class DoppelkopfScoreRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper,
    private val scoreDao: DoppelkopfScorePersistenceAdapter,
    private val roundSequence: RoundSequence,
    private val databaseChanges: DatabaseChanges,
) : DoppelkopfScoresRepository
{
    /** The position is decided inside the write, from what the database holds then. */
    override suspend fun insert(score: DoppelkopfScore, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            dbHelper
                .transaction { db ->
                    val seq = roundSequence.next(db, after = scoreDao.lastSeq(gameId).getOrThrow())
                    scoreDao.insert(DoppelkopfScoreDto.mapFrom(score, gameId, seq)).getOrThrow()
                }
                .onSuccess { databaseChanges.notifyChanged(DataTopic.DOPPELKOPF) }
        }

    override suspend fun update(score: DoppelkopfScore, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            scoreDao.update(DoppelkopfScoreDto.mapFrom(score, gameId, SEQ_IGNORED_ON_UPDATE))
                .onSuccess { databaseChanges.notifyChanged(DataTopic.DOPPELKOPF) }
        }

    override suspend fun deleteLast(gameId: UUID): Result<UUID?> =
        withContext(Dispatchers.IO) {
            dbHelper
                .transaction { scoreDao.softDeleteLast(gameId).getOrThrow() }
                .onSuccess { removed ->
                    if (removed != null) databaseChanges.notifyChanged(DataTopic.DOPPELKOPF)
                }
        }

    private companion object
    {
        const val SEQ_IGNORED_ON_UPDATE = ""
    }
}
