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
import com.damhoe.skatscores.persistence.DatabaseHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
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
) : SkatGamesRepository
{
    private val _allGamesState = MutableStateFlow<List<SkatGame>>(emptyList())
    val allGamesState = _allGamesState.asStateFlow()

    private val TAG = SkatGamesRepositoryImpl::class.java.simpleName

    init
    {
        runBlocking { refreshAll() }
    }

    override suspend fun save(game: SkatGame): Result<Unit>
    {
        val result = dbHelper.transaction {
            val dto = SkatGameDto.mapFrom(game)

            skatGamesDao.insert(dto)

            skatParticipantsPersistenceAdapter.insert(
                SkatParticipantDto.mapManyFrom(
                    game.participants,
                    game.id
                )
            )

        }

        refreshAll()

        return result
    }

    override suspend fun delete(id: UUID): Result<SkatGame?>
    {
        // For delete, consider if it also needs to be transactional
        // if SkatPlayerDto entries related to game.id should also be deleted
        // and if refreshAll should only happen on full success.
        // Current implementation is not fully transactional for all related data.
        return get(id)
            .onSuccess { game ->
                game?.run {
                    skatGamesDao.delete(id)
                        .onSuccess { refreshAll() }
                }
            }
    }

    override suspend fun update(game: SkatGame): Result<Unit>
    {
        val result = dbHelper.transaction {
            val dto = SkatGameDto.mapFrom(game)
            val participantsDto = SkatParticipantDto.mapManyFrom(
                game.participants,
                game.id
            )

            skatGamesDao.update(dto)
            skatParticipantsPersistenceAdapter.update(participantsDto)
        }

        refreshAll()

        return result
    }

    override suspend fun get(id: UUID): Result<SkatGame?>
    {
        return skatGamesDao.get(id)
            .map { skatGameDto ->
                val participants = loadSkatParticipants(id)
                val scores = loadSkatScores(id)
                skatGameDto?.toSkatGame(
                    participants = participants,
                    scores = scores)
            }
    }

    override suspend fun refreshAll()
    {
        skatGamesDao.getAll()
            .onSuccess { dtoList ->
                val fetchedSkatGames = dtoList
                    .map { skatGameDto ->
                        val gameId = skatGameDto.id
                        val participants = loadSkatParticipants(gameId)
                        val scores = loadSkatScores(gameId)
                        skatGameDto.toSkatGame(
                            participants = participants,
                            scores = scores)
                    }

                _allGamesState.value = fetchedSkatGames
            }
            .onFailure { e ->
                Log.e(TAG, "Error refreshing all games", e)
            }
    }

    override fun getAllSince(oldest: Instant): Flow<List<SkatGamePreview>>
    {
        val result = skatGamesDao.getAll()

        return flow {
            result.onSuccess { dtoList ->
                val previewList = dtoList
                    .filter { it.playedAt >= oldest } // filter applied correctly
                    .map {
                        val participants = loadSkatParticipants(it.id)
                        val scores = loadSkatScores(it.id)
                        val skatGame = it.toSkatGame(
                            participants = participants,
                            scores = scores)
                        SkatGamePreview.mapFrom(skatGame)
                    }
                    .sortedByDescending { it.playedAt }
                emit(previewList)
            }.onFailure { e ->
                Log.e(TAG, "Error getting all games since $oldest", e)
                emit(emptyList()) // Emit empty list or handle error as appropriate
            }
        }
    }

    override fun getAll(): Flow<List<SkatGamePreview>>
    {
        val result = skatGamesDao.getAll()

        return flow {
            result.onSuccess { dtoList ->
                val previewList = dtoList
                    .map {
                        // Scores are needed: the preview shows running totals and progress.
                        val participants = loadSkatParticipants(it.id)
                        val scores = loadSkatScores(it.id)
                        val skatGame = it.toSkatGame(
                            participants = participants,
                            scores = scores)
                        SkatGamePreview.mapFrom(skatGame)
                    }
                    .sortedByDescending { it.playedAt }
                emit(previewList)
            }.onFailure { e ->
                Log.e(TAG, "Error getting all games", e)
                emit(emptyList()) // Emit empty list or handle error as appropriate
            }
        }
    }

    private fun loadSkatParticipants(
        gameId: UUID,
    ): SkatParticipants
    {
        val participantDtos = skatParticipantsPersistenceAdapter.getAllOfGame(gameId)
            .getOrElse { e ->
                Log.e(TAG, "Error loading skat players for game $gameId", e)
                emptyList() // Return empty list on error to allow game to load partially or with defaults
            }

        val forehand = participantDtos.find { it.tablePosition == FOREHAND }!!
        val middlehand = participantDtos.find { it.tablePosition == MIDDLEHAND }!!
        val rearhand = participantDtos.find { it.tablePosition == REARHAND }!!

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
