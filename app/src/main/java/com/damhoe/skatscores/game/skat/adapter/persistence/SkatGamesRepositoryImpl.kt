package com.damhoe.skatscores.game.skat.adapter.persistence

import android.util.Log
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.adapter.persistence.players.SkatParticipantDto
import com.damhoe.skatscores.game.skat.adapter.persistence.players.SkatParticipantsPersistenceAdapter
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScoreDto
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScorePersistenceAdapter
import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatPlayerPosition.FOREHAND
import com.damhoe.skatscores.game.skat.domain.SkatPlayerPosition.MIDDLEHAND
import com.damhoe.skatscores.game.skat.domain.SkatPlayerPosition.REARHAND
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.persistence.DataTopic
import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.persistence.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
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

    override suspend fun save(game: SkatGame): Result<Unit> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction {
                skatGamesDao.insert(SkatGameDto.mapFrom(game)).getOrThrow()

                skatParticipantsPersistenceAdapter.insert(
                    SkatParticipantDto.mapManyFrom(game.participants, game.id)
                ).getOrThrow()
            }
            .onSuccess { databaseChanges.notifyChanged(DataTopic.SKAT) }
    }

    /** Soft delete; seats and rounds stay and are hidden with the list. */
    override suspend fun delete(id: UUID): Result<Unit> = withContext(Dispatchers.IO) {
        skatGamesDao.softDelete(id)
            .onSuccess { deleted -> if (deleted) databaseChanges.notifyChanged(DataTopic.SKAT) }
            .map { }
    }

    override suspend fun updateSettings(
        id: UUID,
        title: Title,
        settings: SkatSettings,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        skatGamesDao
            .updateSettings(
                id = id,
                title = title.value,
                roundCount = settings.roundCount.value,
                scoringMode = settings.scoringMode,
            )
            .onSuccess { databaseChanges.notifyChanged(DataTopic.SKAT) }
    }

    override suspend fun updateSeats(
        id: UUID,
        participants: SkatParticipants,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction {
                skatParticipantsPersistenceAdapter
                    .replaceSeats(SkatParticipantDto.mapManyFrom(participants, id))
                    .getOrThrow()
            }
            .onSuccess { databaseChanges.notifyChanged(DataTopic.SKAT) }
    }

    override suspend fun get(id: UUID): Result<SkatGame?> = withContext(Dispatchers.IO) {
        load(id)
    }

    override fun observe(id: UUID): Flow<SkatGame?> =
        databaseChanges.changes(DataTopic.SKAT)
            .mapLatest { load(id).getOrNull() }
            .flowOn(Dispatchers.IO)

    /** Null also when the seats are incomplete. Domain validation failures become failures. */
    private fun load(id: UUID): Result<SkatGame?> = dbHelper
        .read {
            val game = skatGamesDao.get(id).getOrThrow() ?: return@read null
            val participants = skatParticipantsPersistenceAdapter.getAllOfGame(id).getOrThrow()
            val scores = skatScoresPersistenceAdapter.getScoresForGame(id).getOrThrow()
            Triple(game, participants, scores)
        }
        .mapCatching { rows ->
            val (game, participants, scores) = rows ?: return@mapCatching null
            val seats = toSkatParticipants(id, participants) ?: return@mapCatching null
            game.toSkatGame(participants = seats, scores = scores.map { it.toSkatScore() })
        }

    override fun getAllSince(oldest: Instant): Flow<List<SkatGamePreview>> = previews(oldest)

    override fun getAll(): Flow<List<SkatGamePreview>> = previews(since = null)

    private fun previews(since: Instant?): Flow<List<SkatGamePreview>> =
        databaseChanges.changes(DataTopic.SKAT)
            .mapLatest { loadPreviews(since) }
            .flowOn(Dispatchers.IO)

    /** Three queries in total, grouped here, instead of two per list. */
    private fun loadPreviews(since: Instant?): List<SkatGamePreview> = dbHelper
        .read {
            Triple(
                skatGamesDao.getAll(since).getOrThrow(),
                skatParticipantsPersistenceAdapter.getAllOfLiveGames(since).getOrThrow()
                    .groupBy { it.gameId },
                skatScoresPersistenceAdapter.getScoresOfLiveGames(since).getOrThrow()
                    .groupBy { it.gameId },
            )
        }
        .map { (games, participantsByGame, scoresByGame) ->
            games.mapNotNull { dto -> toPreview(dto, participantsByGame, scoresByGame) }
        }
        .getOrElse { e ->
            Log.e(TAG, "Error getting all games", e)
            emptyList()
        }

    /** Null for an unreadable list, so one bad list does not empty the home screen. */
    private fun toPreview(
        dto: SkatGameDto,
        participantsByGame: Map<UUID, List<SkatParticipantDto>>,
        scoresByGame: Map<UUID, List<SkatScoreDto>>,
    ): SkatGamePreview? = runCatching {
        val seats = toSkatParticipants(dto.id, participantsByGame[dto.id].orEmpty())
            ?: return null
        val scores = scoresByGame[dto.id].orEmpty().map { it.toSkatScore() }
        SkatGamePreview.mapFrom(dto.toSkatGame(participants = seats, scores = scores))
    }.getOrElse { e ->
        Log.e(TAG, "Could not read game ${dto.id}, skipping it", e)
        null
    }

    private fun toSkatParticipants(
        gameId: UUID,
        participantDtos: List<SkatParticipantDto>,
    ): SkatParticipants?
    {
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
}
