package com.damhoe.skatscores.game.skat.adapter.persistence

import android.util.Log
import com.damhoe.skatscores.game.skat.adapter.persistence.players.SkatParticipantDto
import com.damhoe.skatscores.game.skat.adapter.persistence.players.SkatParticipantsPersistenceAdapter
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScorePersistenceAdapter
import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatPlayerPosition.FOREHAND
import com.damhoe.skatscores.game.skat.domain.SkatPlayerPosition.MIDDLEHAND
import com.damhoe.skatscores.game.skat.domain.SkatPlayerPosition.REARHAND
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.persistence.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkatGamesRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper,
    private val skatGamesDao: SkatGamesPersistenceAdapter,
    private val skatParticipantsPersistenceAdapter: SkatParticipantsPersistenceAdapter,
    private val skatScoresPersistenceAdapter: SkatScorePersistenceAdapter,
    private val databaseChanges: DatabaseChanges,
) : SkatGamesRepository
{
    private val TAG = SkatGamesRepositoryImpl::class.java.simpleName

    /*
     * Every suspending call here hops to IO. The adapters below are blocking, and these are
     * called from viewModelScope, whose dispatcher is the main thread - so without this,
     * opening a list or saving a round was database work on the thread drawing the frame.
     * The preview flows do the same with flowOn, further down.
     */

    /**
     * A list and its seats go in together or not at all: getOrThrow is what makes the
     * transaction roll back, and a list written without its seats cannot be read back.
     */
    override suspend fun save(game: SkatGame): Result<Unit> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction {
                skatGamesDao.insert(SkatGameDto.mapFrom(game)).getOrThrow()

                skatParticipantsPersistenceAdapter.insert(
                    SkatParticipantDto.mapManyFrom(game.participants, game.id)
                ).getOrThrow()
            }
            .onSuccess { databaseChanges.notifyChanged() }
    }

    /**
     * Takes the seats and the rounds with it. The foreign keys declare ON DELETE CASCADE, but
     * SQLite only honours that with foreign key enforcement switched on, which it is not by
     * default on Android - so the children are removed here rather than assumed away. Left
     * behind, the participant rows would keep counting towards the player statistics of a
     * list that no longer exists.
     */
    override suspend fun delete(id: UUID): Result<SkatGame?> = withContext(Dispatchers.IO) {
        val game = get(id).getOrElse { return@withContext Result.failure(it) }
            ?: return@withContext Result.success(null)

        dbHelper
            .transaction {
                skatScoresPersistenceAdapter.deleteAllOfGame(id).getOrThrow()
                skatParticipantsPersistenceAdapter.deleteAllOfGame(id).getOrThrow()
                skatGamesDao.delete(id).getOrThrow()
            }
            .onSuccess { databaseChanges.notifyChanged() }
            // Half a deletion is worse than none: a failure here rolls the whole thing back
            // and says so, rather than reporting the list gone while its rounds remain.
            .map { game }
    }

    override suspend fun update(game: SkatGame): Result<Unit> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction {
                skatGamesDao.update(SkatGameDto.mapFrom(game)).getOrThrow()

                skatParticipantsPersistenceAdapter.update(
                    SkatParticipantDto.mapManyFrom(game.participants, game.id)
                ).getOrThrow()
            }
            .onSuccess { databaseChanges.notifyChanged() }
    }

    override suspend fun get(id: UUID): Result<SkatGame?> = withContext(Dispatchers.IO) {
        skatGamesDao.get(id)
            .map { skatGameDto ->
                // A list whose seats cannot be read reads as no list at all: the screens
                // that ask for one already handle a null by leaving.
                val participants = loadSkatParticipants(id) ?: return@map null
                skatGameDto?.toSkatGame(
                    participants = participants,
                    scores = loadSkatScores(id))
            }
    }

    override fun getAllSince(oldest: Instant): Flow<List<SkatGamePreview>> =
        previews { it.playedAt >= oldest }

    override fun getAll(): Flow<List<SkatGamePreview>> = previews { true }

    /**
     * Re-read on every change rather than once when this was called: the home screen keeps one
     * LiveData for the whole time it is open, so a list started, played or deleted since then
     * has to arrive on the same flow.
     */
    private fun previews(keep: (SkatGameDto) -> Boolean): Flow<List<SkatGamePreview>> =
        databaseChanges.revision
            .map { loadPreviews(keep) }
            .flowOn(Dispatchers.IO)

    private fun loadPreviews(keep: (SkatGameDto) -> Boolean): List<SkatGamePreview> =
        skatGamesDao.getAll().fold(
            onSuccess = { dtoList ->
                dtoList
                    .filter(keep)
                    // mapNotNull, so one unreadable list is left out of the home screen
                    // rather than emptying it.
                    .mapNotNull {
                        // Scores are needed: the preview shows running totals and progress.
                        val participants = loadSkatParticipants(it.id) ?: return@mapNotNull null
                        val skatGame = it.toSkatGame(
                            participants = participants,
                            scores = loadSkatScores(it.id))
                        SkatGamePreview.mapFrom(skatGame)
                    }
                    .sortedByDescending { it.playedAt }
            },
            onFailure = { e ->
                Log.e(TAG, "Error getting all games", e)
                emptyList()
            }
        )

    /**
     * The three seats of a list, or null if they cannot be read.
     *
     * A seat missing used to be an assertion, which turned a list saved without its seats -
     * or a failed read - into an NPE thrown inside the previews flow, taking the home screen
     * down with it rather than the one list it could not read. Saves are atomic now, so this
     * should not happen; a list that predates that, or a read that fails, is skipped instead
     * of being fatal.
     */
    private fun loadSkatParticipants(
        gameId: UUID,
    ): SkatParticipants?
    {
        val participantDtos = skatParticipantsPersistenceAdapter.getAllOfGame(gameId)
            .getOrElse { e ->
                Log.e(TAG, "Error loading skat players for game $gameId", e)
                emptyList()
            }

        val forehand = participantDtos.find { it.tablePosition == FOREHAND }
        val middlehand = participantDtos.find { it.tablePosition == MIDDLEHAND }
        val rearhand = participantDtos.find { it.tablePosition == REARHAND }

        if (forehand == null || middlehand == null || rearhand == null)
        {
            Log.e(TAG, "Game $gameId is missing seats, skipping it")
            return null
        }

        return SkatParticipants(
            foreHand = forehand.toSkatParticipant(),
            middleHand = middlehand.toSkatParticipant(),
            rearHand = rearhand.toSkatParticipant(),
        )
    }

    private fun loadSkatScores(
        gameId: UUID,
    ): List<SkatScore>
    {
        val scoresDtos = skatScoresPersistenceAdapter.getScoresForGame(gameId)
        return scoresDtos.map { it.toSkatScore() }
    }
}
