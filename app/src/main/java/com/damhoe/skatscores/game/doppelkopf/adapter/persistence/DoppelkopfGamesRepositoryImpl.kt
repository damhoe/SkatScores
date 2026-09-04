package com.damhoe.skatscores.game.doppelkopf.adapter.persistence

import android.util.Log
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.players.DoppelkopfParticipantDto
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.players.DoppelkopfParticipantsPersistenceAdapter
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores.DoppelkopfScorePersistenceAdapter
import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfGamesRepository
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGamePreview
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.persistence.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DoppelkopfGamesRepo"

@Singleton
class DoppelkopfGamesRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper,
    private val gamesDao: DoppelkopfGamesPersistenceAdapter,
    private val participantsDao: DoppelkopfParticipantsPersistenceAdapter,
    private val scoresDao: DoppelkopfScorePersistenceAdapter,
    private val databaseChanges: DatabaseChanges,
) : DoppelkopfGamesRepository
{
    /*
     * Every suspending call here hops to IO. The adapters below are blocking, and these are
     * called from viewModelScope, whose dispatcher is the main thread - so without this,
     * opening a list or saving a round was database work on the thread drawing the frame.
     * The preview flow does the same with flowOn, further down.
     */

    /**
     * A list and its seats go in together or not at all: getOrThrow is what makes the
     * transaction roll back, and a list written without its seats cannot be read back.
     */
    override suspend fun save(game: DoppelkopfGame): Result<Unit> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction {
                gamesDao.insert(DoppelkopfGameDto.mapFrom(game)).getOrThrow()
                participantsDao.insert(
                    DoppelkopfParticipantDto.mapManyFrom(game.participants, game.id)
                ).getOrThrow()
            }
            .onSuccess { databaseChanges.notifyChanged() }
    }

    /**
     * Takes the seats and the rounds with it. The foreign keys declare ON DELETE CASCADE, but
     * SQLite only honours that with foreign key enforcement switched on, which it is not by
     * default on Android - so the children are removed here rather than assumed away.
     */
    override suspend fun delete(id: UUID): Result<DoppelkopfGame?> = withContext(Dispatchers.IO) {
        val game = get(id).getOrElse { return@withContext Result.failure(it) }
            ?: return@withContext Result.success(null)

        dbHelper
            .transaction {
                scoresDao.deleteAllOfGame(id).getOrThrow()
                participantsDao.deleteAllOfGame(id).getOrThrow()
                gamesDao.delete(id).getOrThrow()
            }
            .onSuccess { databaseChanges.notifyChanged() }
            // Half a deletion is worse than none: a failure here rolls the whole thing back
            // and says so, rather than reporting the list gone while its rounds remain.
            .map { game }
    }

    override suspend fun update(game: DoppelkopfGame): Result<Unit> =
        withContext(Dispatchers.IO) {
            dbHelper
                .transaction {
                    gamesDao.update(DoppelkopfGameDto.mapFrom(game)).getOrThrow()
                    participantsDao.update(
                        DoppelkopfParticipantDto.mapManyFrom(game.participants, game.id)
                    ).getOrThrow()
                }
                .onSuccess { databaseChanges.notifyChanged() }
        }

    override suspend fun get(id: UUID): Result<DoppelkopfGame?> = withContext(Dispatchers.IO) {
        gamesDao.get(id)
            // mapCatching, not map: the row still has to be turned into a game, and a stored
            // value the domain refuses belongs in the Result rather than thrown at the caller.
            .mapCatching { dto ->
                // A list whose seats or rounds cannot be read reads as no list at all: the
                // screens that ask for one already handle a null by leaving.
                val participants = loadParticipants(id) ?: return@mapCatching null
                val scores = loadScores(id) ?: return@mapCatching null
                dto?.toDoppelkopfGame(
                    participants = participants,
                    scores = scores,
                )
            }
    }

    /**
     * Re-read on every change rather than once when this was called: the home screen keeps one
     * LiveData for the whole time it is open, so a list started, played or deleted since then
     * has to arrive on the same flow.
     */
    override fun getAll(): Flow<List<DoppelkopfGamePreview>> = databaseChanges.revision
        .map { loadPreviews() }
        .flowOn(Dispatchers.IO)

    private fun loadPreviews(): List<DoppelkopfGamePreview> = gamesDao.getAll().fold(
        onSuccess = { dtoList ->
            dtoList
                // mapNotNull, so one unreadable list is left out of the home screen rather
                // than emptying it.
                .mapNotNull { dto ->
                    val participants = loadParticipants(dto.id) ?: return@mapNotNull null
                    // Scores are needed: the preview shows running totals and progress.
                    val scores = loadScores(dto.id) ?: return@mapNotNull null

                    // The domain can refuse a stored row too - a round count out of range, a
                    // title that no longer validates - and that has to cost the one list
                    // rather than the screen.
                    runCatching {
                        DoppelkopfGamePreview.mapFrom(
                            dto.toDoppelkopfGame(
                                participants = participants,
                                scores = scores,
                            )
                        )
                    }.getOrElse { e ->
                        Log.e(TAG, "Could not read game ${dto.id}, skipping it", e)
                        null
                    }
                }
                .sortedByDescending { it.playedAt }
        },
        onFailure = { e ->
            Log.e(TAG, "Error getting all Doppelkopf games", e)
            emptyList()
        }
    )

    /**
     * The four seats of a list, or null if they cannot be read.
     *
     * This used to fall back to placeholder seats, which kept the screen alive but showed a
     * real list under invented names. Skipping it says the same thing honestly, and matches
     * what the Skat side does.
     */
    private fun loadParticipants(gameId: UUID): DoppelkopfParticipants?
    {
        val dtos = participantsDao.getAllOfGame(gameId)
            .getOrElse { e ->
                Log.e(TAG, "Error loading participants for game $gameId", e)
                emptyList()
            }

        if (dtos.size != DoppelkopfParticipants.SEAT_COUNT)
        {
            Log.e(TAG, "Game $gameId has ${dtos.size} seats, expected 4, skipping it")
            return null
        }

        return DoppelkopfParticipants(dtos.sortedBy { it.seat }.map { it.toParticipant() })
    }

    /**
     * The rounds of a list, or null if they cannot be read.
     *
     * Empty is not the fallback: a list whose rounds failed to load would otherwise be shown
     * with everybody on zero, which reads as a real standing rather than as a read that did
     * not work.
     */
    private fun loadScores(gameId: UUID): List<DoppelkopfScore>? =
        scoresDao.getScoresForGame(gameId)
            .mapCatching { dtos -> dtos.map { it.toDoppelkopfScore() } }
            .getOrElse { e ->
                Log.e(TAG, "Error loading rounds for game $gameId", e)
                null
            }
}
