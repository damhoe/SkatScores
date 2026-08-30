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
    override suspend fun save(game: DoppelkopfGame): Result<Unit> = dbHelper.transaction {
        gamesDao.insert(DoppelkopfGameDto.mapFrom(game))
        participantsDao.insert(DoppelkopfParticipantDto.mapManyFrom(game.participants, game.id))
    }.also { databaseChanges.notifyChanged() }

    /**
     * Takes the seats and the rounds with it. The foreign keys declare ON DELETE CASCADE, but
     * SQLite only honours that with foreign key enforcement switched on, which it is not by
     * default on Android - so the children are removed here rather than assumed away.
     */
    override suspend fun delete(id: UUID): Result<DoppelkopfGame?> = get(id)
        .onSuccess { game ->
            game ?: return@onSuccess

            dbHelper.transaction {
                scoresDao.deleteAllOfGame(id)
                participantsDao.deleteAllOfGame(id)
                gamesDao.delete(id)
            }

            databaseChanges.notifyChanged()
        }

    override suspend fun update(game: DoppelkopfGame): Result<Unit> = dbHelper.transaction {
        gamesDao.update(DoppelkopfGameDto.mapFrom(game))
        participantsDao.update(DoppelkopfParticipantDto.mapManyFrom(game.participants, game.id))
    }.also { databaseChanges.notifyChanged() }

    override suspend fun get(id: UUID): Result<DoppelkopfGame?> = gamesDao.get(id)
        .map { dto ->
            dto?.toDoppelkopfGame(
                participants = loadParticipants(id),
                scores = loadScores(id),
            )
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
                .map { dto ->
                    // Scores are needed: the preview shows running totals and progress.
                    val game = dto.toDoppelkopfGame(
                        participants = loadParticipants(dto.id),
                        scores = loadScores(dto.id),
                    )
                    DoppelkopfGamePreview.mapFrom(game)
                }
                .sortedByDescending { it.playedAt }
        },
        onFailure = { e ->
            Log.e(TAG, "Error getting all Doppelkopf games", e)
            emptyList()
        }
    )

    private fun loadParticipants(gameId: UUID): DoppelkopfParticipants
    {
        val dtos = participantsDao.getAllOfGame(gameId)
            .getOrElse { e ->
                Log.e(TAG, "Error loading participants for game $gameId", e)
                emptyList()
            }

        // A list whose seats could not be read is unusable, so fall back to placeholders
        // rather than crashing the screen that asked for it.
        if (dtos.size != DoppelkopfParticipants.SEAT_COUNT)
        {
            Log.e(TAG, "Game $gameId has ${dtos.size} seats, expected 4")
            return DoppelkopfParticipants.createNew()
        }

        return DoppelkopfParticipants(dtos.sortedBy { it.seat }.map { it.toParticipant() })
    }

    private fun loadScores(gameId: UUID): List<DoppelkopfScore> =
        scoresDao.getScoresForGame(gameId).map { it.toDoppelkopfScore() }
}
