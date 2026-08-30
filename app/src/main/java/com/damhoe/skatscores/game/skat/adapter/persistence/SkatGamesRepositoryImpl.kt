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

        databaseChanges.notifyChanged()

        return result
    }

    /**
     * Takes the seats and the rounds with it. The foreign keys declare ON DELETE CASCADE, but
     * SQLite only honours that with foreign key enforcement switched on, which it is not by
     * default on Android - so the children are removed here rather than assumed away. Left
     * behind, the participant rows would keep counting towards the player statistics of a
     * list that no longer exists.
     */
    override suspend fun delete(id: UUID): Result<SkatGame?>
    {
        return get(id)
            .onSuccess { game ->
                game ?: return@onSuccess

                dbHelper.transaction {
                    skatScoresPersistenceAdapter.deleteAllOfGame(id)
                    skatParticipantsPersistenceAdapter.deleteAllOfGame(id)
                    skatGamesDao.delete(id)
                }

                databaseChanges.notifyChanged()
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

        databaseChanges.notifyChanged()

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
            },
            onFailure = { e ->
                Log.e(TAG, "Error getting all games", e)
                emptyList()
            }
        )

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
