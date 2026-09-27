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

    /** Soft delete; seats and rounds stay and are hidden with the list. */
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
            .mapLatest { load(id).getOrNull() }
            .flowOn(Dispatchers.IO)

    /** Null also when the seats are incomplete. Domain validation failures become failures. */
    private fun load(id: UUID): Result<DoppelkopfGame?> = dbHelper
        .read {
            val game = gamesDao.get(id).getOrThrow() ?: return@read null
            val participants = participantsDao.getAllOfGame(id).getOrThrow()
            val scores = scoresDao.getScoresForGame(id).getOrThrow()
            Triple(game, participants, scores)
        }
        .mapCatching { rows ->
            val (game, participants, scores) = rows ?: return@mapCatching null
            val seats = toParticipants(id, participants) ?: return@mapCatching null
            game.toDoppelkopfGame(
                participants = seats,
                scores = scores.map { it.toDoppelkopfScore() },
            )
        }

    override fun getAll(): Flow<List<DoppelkopfGamePreview>> =
        databaseChanges.changes(DataTopic.DOPPELKOPF)
            .mapLatest { loadPreviews() }
            .flowOn(Dispatchers.IO)

    /** Three queries in total, grouped here, instead of two per list. */
    private fun loadPreviews(): List<DoppelkopfGamePreview> = dbHelper
        .read {
            Triple(
                gamesDao.getAll().getOrThrow(),
                participantsDao.getAllOfLiveGames().getOrThrow().groupBy { it.gameId },
                scoresDao.getScoresOfLiveGames().getOrThrow().groupBy { it.gameId },
            )
        }
        .map { (games, participantsByGame, scoresByGame) ->
            games.mapNotNull { dto -> toPreview(dto, participantsByGame, scoresByGame) }
        }
        .getOrElse { e ->
            Log.e(TAG, "Error getting all Doppelkopf games", e)
            emptyList()
        }

    /** Null for an unreadable list, so one bad list does not empty the home screen. */
    private fun toPreview(
        dto: DoppelkopfGameDto,
        participantsByGame: Map<UUID, List<DoppelkopfParticipantDto>>,
        scoresByGame: Map<UUID, List<DoppelkopfScoreDto>>,
    ): DoppelkopfGamePreview? = runCatching {
        val seats = toParticipants(dto.id, participantsByGame[dto.id].orEmpty()) ?: return null
        val scores = scoresByGame[dto.id].orEmpty().map { it.toDoppelkopfScore() }
        DoppelkopfGamePreview.mapFrom(dto.toDoppelkopfGame(participants = seats, scores = scores))
    }.getOrElse { e ->
        Log.e(TAG, "Could not read game ${dto.id}, skipping it", e)
        null
    }

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
