package com.damhoe.skatscores.game.doppelkopf.application.usecases

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfGamesRepository
import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfScoresRepository
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGamePreview
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSettings
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.util.UUID
import javax.inject.Inject

/**
 * The Doppelkopf use cases, gathered the way [com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases]
 * gathers Skat's.
 *
 * As there, deleting a single round is deliberately absent: a round is only ever removed
 * through [RemoveLastDoppelkopfScoreUseCase], so the round numbering cannot develop holes.
 */
data class DoppelkopfGameUseCases @Inject constructor(
    val getGame: GetDoppelkopfGameUseCase,
    val createGame: CreateDoppelkopfGameUseCase,
    val deleteGame: DeleteDoppelkopfGameUseCase,
    val updateGame: UpdateDoppelkopfGameUseCase,
    val updateParticipants: UpdateDoppelkopfParticipantsUseCase,
    val addScore: AddScoreToDoppelkopfGameUseCase,
    val updateScore: UpdateDoppelkopfScoreUseCase,
    val removeLastScore: RemoveLastDoppelkopfScoreUseCase,
    val getPreviews: GetDoppelkopfGamePreviewsUseCase,
)

@Parcelize
data class CreateDoppelkopfGameCommand(
    val settings: DoppelkopfSettings,
    val title: Title,
    /** Who sits at the table. Null means start with placeholder guests. */
    val participants: DoppelkopfParticipants? = null,
) : Parcelable

class CreateDoppelkopfGameUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
)
{
    suspend operator fun invoke(command: CreateDoppelkopfGameCommand): Result<DoppelkopfGame> =
        withContext(Dispatchers.IO) {
            val game = DoppelkopfGame.create(
                command.title,
                command.settings,
                command.participants ?: DoppelkopfParticipants.createNew(),
            )

            repository.save(game).map { game }
        }
}

class GetDoppelkopfGameUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
)
{
    suspend operator fun invoke(id: UUID): Result<DoppelkopfGame?> = repository.get(id)
}

class DeleteDoppelkopfGameUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
)
{
    suspend operator fun invoke(id: UUID): Result<DoppelkopfGame?> =
        withContext(Dispatchers.IO) { repository.delete(id) }
}

class GetDoppelkopfGamePreviewsUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
)
{
    operator fun invoke(): LiveData<List<DoppelkopfGamePreview>> = repository.getAll().asLiveData()
}

data class UpdateDoppelkopfGameCommand(
    val id: UUID,
    val title: Title,
    val settings: DoppelkopfSettings,
)

class UpdateDoppelkopfGameUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
    private val getGame: GetDoppelkopfGameUseCase,
)
{
    suspend operator fun invoke(command: UpdateDoppelkopfGameCommand): Result<DoppelkopfGame> =
        getGame(command.id).fold(
            onSuccess = { game ->
                if (game == null)
                {
                    return Result.failure(Exception("Game not found with ID: ${command.id}"))
                }

                val updated = game.copy(title = command.title, settings = command.settings)
                repository.update(updated).map { updated }
            },
            onFailure = { Result.failure(it) },
        )
}

data class UpdateDoppelkopfParticipantsCommand(
    val id: UUID,
    val participants: DoppelkopfParticipants,
)

class UpdateDoppelkopfParticipantsUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
    private val getGame: GetDoppelkopfGameUseCase,
)
{
    suspend operator fun invoke(
        command: UpdateDoppelkopfParticipantsCommand,
    ): Result<DoppelkopfGame> = getGame(command.id).fold(
        onSuccess = { game ->
            if (game == null)
            {
                return Result.failure(Exception("Game not found with ID: ${command.id}"))
            }

            // A seat keeps its participant id even when another person takes it over,
            // otherwise the rounds already recorded lose their score board column.
            val seats: List<Participant> = command.participants.asList()
                .mapIndexed { seat, participant ->
                    participant.withId(game.participants.asList()[seat].id)
                }

            val updated = game.copy(participants = DoppelkopfParticipants(seats))
            repository.update(updated).map { updated }
        },
        onFailure = { Result.failure(it) },
    )
}

class AddScoreToDoppelkopfGameUseCase @Inject constructor(
    private val scoreRepository: DoppelkopfScoresRepository,
)
{
    suspend operator fun invoke(
        game: DoppelkopfGame,
        score: DoppelkopfScore,
    ): Result<DoppelkopfGame>
    {
        val result = game.addScore(score)

        return scoreRepository
            .insert(score = result.score, gameId = game.id, round = result.round)
            .map { result.updatedGame }
    }
}

class UpdateDoppelkopfScoreUseCase @Inject constructor(
    private val scoreRepository: DoppelkopfScoresRepository,
)
{
    suspend operator fun invoke(
        game: DoppelkopfGame,
        score: DoppelkopfScore,
    ): Result<DoppelkopfGame> = scoreRepository
        .update(score = score, gameId = game.id)
        .map { game.updateScore(score) }
}

/** Undo for the bottom bar: drops the most recently played round. */
class RemoveLastDoppelkopfScoreUseCase @Inject constructor(
    private val scoreRepository: DoppelkopfScoresRepository,
)
{
    suspend operator fun invoke(game: DoppelkopfGame): Result<DoppelkopfGame>
    {
        val lastScore = game.scores.lastOrNull() ?: return Result.success(game)

        return scoreRepository
            .delete(id = lastScore.id, gameId = game.id)
            .map { game.removeScore(lastScore.id) }
    }
}
