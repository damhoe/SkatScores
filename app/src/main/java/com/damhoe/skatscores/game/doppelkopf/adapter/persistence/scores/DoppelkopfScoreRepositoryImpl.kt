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
    /*
     * Every suspending call here hops to IO. The adapter below is blocking, and these are
     * called from viewModelScope, whose dispatcher is the main thread - so without this,
     * writing a round was database work on the thread drawing the frame.
     */

    /**
     * Appends a round. Where it goes is decided here, inside the write, from what the database
     * holds at that moment - not from a round count a screen read earlier.
     */
    override suspend fun insert(score: DoppelkopfScore, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            dbHelper
                .transaction { db ->
                    val seq = roundSequence.next(db, after = scoreDao.lastSeq(gameId).getOrThrow())
                    scoreDao.insert(DoppelkopfScoreDto.mapFrom(score, gameId, seq)).getOrThrow()
                }
                .onSuccess { databaseChanges.notifyChanged(DataTopic.DOPPELKOPF) }
        }

    /** Replaces a round with an edited version; it keeps its place in the list. */
    override suspend fun update(score: DoppelkopfScore, gameId: UUID): Result<Unit> =
        withContext(Dispatchers.IO) {
            // The seq is not written on an edit, so any value does here.
            scoreDao.update(DoppelkopfScoreDto.mapFrom(score, gameId, seq = ""))
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
}
