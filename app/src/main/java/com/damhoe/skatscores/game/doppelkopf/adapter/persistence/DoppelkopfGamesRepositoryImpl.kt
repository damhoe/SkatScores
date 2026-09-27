package com.damhoe.skatscores.game.doppelkopf.adapter.persistence

import android.util.Log
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.players.DoppelkopfParticipantDto
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.players.DoppelkopfParticipantsPersistenceAdapter
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores.DoppelkopfScoreDto
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores.DoppelkopfScorePersistenceAdapter
import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfGamesRepository
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGamePreview
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSettings
import com.damhoe.skatscores.persistence.DataTopic
import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.persistence.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DoppelkopfGamesRepo"

@OptIn(ExperimentalCoroutinesApi::class)
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
     * The flows do the same with flowOn, further down.
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
            .onSuccess { databaseChanges.notifyChanged(DataTopic.DOPPELKOPF) }
    }

    /**
     * Marks the list deleted. Its seats and rounds stay in place under it, and every read
     * already leaves out what belongs to a deleted list - the player statistics included.
     */
    override suspend fun delete(id: UUID): Result<Unit> = withContext(Dispatchers.IO) {
        gamesDao.softDelete(id)
            .onSuccess { deleted ->
                if (deleted) databaseChanges.notifyChanged(DataTopic.DOPPELKOPF)
            }
            .map { }
    }

    override suspend fun updateSettings(
        id: UUID,
        title: Title,
        settings: DoppelkopfSettings,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        gamesDao
            .updateSettings(
                id = id,
                title = title.value,
                roundCount = settings.roundCount.value,
                scoringMode = settings.scoringMode,
            )
            .onSuccess { databaseChanges.notifyChanged(DataTopic.DOPPELKOPF) }
    }

    override suspend fun updateSeats(
        id: UUID,
        participants: DoppelkopfParticipants,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction {
                participantsDao
                    .replaceSeats(DoppelkopfParticipantDto.mapManyFrom(participants, id))
                    .getOrThrow()
            }
            .onSuccess { databaseChanges.notifyChanged(DataTopic.DOPPELKOPF) }
    }

    override suspend fun get(id: UUID): Result<DoppelkopfGame?> = withContext(Dispatchers.IO) {
        load(id)
    }

    override fun observe(id: UUID): Flow<DoppelkopfGame?> =
        databaseChanges.changes(DataTopic.DOPPELKOPF)
            // Latest, not every: a read made stale by a newer write is dropped, never shown.
            .mapLatest { load(id).getOrNull() }
            .flowOn(Dispatchers.IO)

    /** The list, its seats and its rounds, read from one snapshot. */
    private fun load(id: UUID): Result<DoppelkopfGame?> = dbHelper
        .read {
            val game = gamesDao.get(id).getOrThrow() ?: return@read null
            val participants = participantsDao.getAllOfGame(id).getOrThrow()
            val scores = scoresDao.getScoresForGame(id).getOrThrow()
            Triple(game, participants, scores)
        }
        .mapCatching { rows ->
            val (game, participants, scores) = rows ?: return@mapCatching null
            // A list whose seats cannot be read reads as no list at all: the screens that ask
            // for one already handle a null by leaving.
            val seats = toParticipants(id, participants) ?: return@mapCatching null
            game.toDoppelkopfGame(
                participants = seats,
                scores = scores.map { it.toDoppelkopfScore() },
            )
        }

    /**
     * Re-read on every change rather than once when this was called: the home screen keeps one
     * LiveData for the whole time it is open, so a list started, played or deleted since then
     * has to arrive on the same flow.
     */
    override fun getAll(): Flow<List<DoppelkopfGamePreview>> =
        databaseChanges.changes(DataTopic.DOPPELKOPF)
            .mapLatest { loadPreviews() }
            .flowOn(Dispatchers.IO)

    /** Three queries for the whole home screen, read from one snapshot. */
    private fun loadPreviews(): List<DoppelkopfGamePreview> = dbHelper
        .read {
            Triple(
                gamesDao.getAll().getOrThrow(),
                participantsDao.getAllOfLiveGames().getOrThrow().groupBy { it.gameId },
                scoresDao.getScoresOfLiveGames().getOrThrow().groupBy { it.gameId },
            )
        }
        .map { (games, participantsByGame, scoresByGame) ->
            // mapNotNull, so one unreadable list is left out of the home screen rather than
            // emptying it.
            games.mapNotNull { dto -> toPreview(dto, participantsByGame, scoresByGame) }
        }
        .getOrElse { e ->
            Log.e(TAG, "Error getting all Doppelkopf games", e)
            emptyList()
        }

    private fun toPreview(
        dto: DoppelkopfGameDto,
        participantsByGame: Map<UUID, List<DoppelkopfParticipantDto>>,
        scoresByGame: Map<UUID, List<DoppelkopfScoreDto>>,
    ): DoppelkopfGamePreview? = runCatching {
        val seats = toParticipants(dto.id, participantsByGame[dto.id].orEmpty()) ?: return null
        val scores = scoresByGame[dto.id].orEmpty().map { it.toDoppelkopfScore() }
        DoppelkopfGamePreview.mapFrom(dto.toDoppelkopfGame(participants = seats, scores = scores))
    }.getOrElse { e ->
        // The domain can refuse a stored row too - a round count out of range, a title that
        // no longer validates - and that has to cost the one list rather than the screen.
        Log.e(TAG, "Could not read game ${dto.id}, skipping it", e)
        null
    }

    /**
     * The four seats of a list, or null if they are not all there.
     *
     * This used to fall back to placeholder seats, which kept the screen alive but showed a
     * real list under invented names. Skipping it says the same thing honestly.
     */
    private fun toParticipants(
        gameId: UUID,
        dtos: List<DoppelkopfParticipantDto>,
    ): DoppelkopfParticipants?
    {
        if (dtos.size != DoppelkopfParticipants.SEAT_COUNT)
        {
            Log.e(TAG, "Game $gameId has ${dtos.size} seats, expected 4, skipping it")
            return null
        }

        return DoppelkopfParticipants(dtos.sortedBy { it.seat }.map { it.toParticipant() })
    }
}
