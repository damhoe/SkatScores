package com.damhoe.skatscores.game.doppelkopf.application.usecases

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfGamesRepository
import com.damhoe.skatscores.game.doppelkopf.application.repository.DoppelkopfScoresRepository
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGamePreview
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSettings
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.util.UUID
import javax.inject.Inject

/**
 * The Doppelkopf use cases, gathered the way [com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases]
 * gathers Skat's.
 *
 * As there, deleting a single round is deliberately absent: a round is only ever removed
 * through [RemoveLastDoppelkopfScoreUseCase].
 */
data class DoppelkopfGameUseCases @Inject constructor(
    val getGame: GetDoppelkopfGameUseCase,
    val observeGame: ObserveDoppelkopfGameUseCase,
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

/**
 * A list as it is now and after every write to it, so a screen never works from a copy that
 * is out of date. Null once the list is gone.
 */
class ObserveDoppelkopfGameUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
)
{
    operator fun invoke(id: UUID): Flow<DoppelkopfGame?> = repository.observe(id)
}

class DeleteDoppelkopfGameUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
)
{
    suspend operator fun invoke(id: UUID): Result<Unit> = repository.delete(id)
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
)
{
    suspend operator fun invoke(command: UpdateDoppelkopfGameCommand): Result<Unit> =
        repository.updateSettings(command.id, command.title, command.settings)
}

data class UpdateDoppelkopfParticipantsCommand(
    val id: UUID,
    val participants: DoppelkopfParticipants,
)

/**
 * Changes who sits at the seats of a running list. A change is a correction: each seat keeps
 * its participant id, so the rounds already recorded are credited to the name now on it.
 */
class UpdateDoppelkopfParticipantsUseCase @Inject constructor(
    private val repository: DoppelkopfGamesRepository,
)
{
    suspend operator fun invoke(command: UpdateDoppelkopfParticipantsCommand): Result<Unit> =
        repository.updateSeats(command.id, command.participants)
}

class AddScoreToDoppelkopfGameUseCase @Inject constructor(
    private val scoreRepository: DoppelkopfScoresRepository,
)
{
    /** Appends [score] as the newest round; where it lands is the database's call. */
    suspend operator fun invoke(
        game: DoppelkopfGame,
        score: DoppelkopfScore,
    ): Result<DoppelkopfGame> = scoreRepository
        .insert(score = score, gameId = game.id)
        .map { game.addScore(score).updatedGame }
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

/**
 * Undo for the bottom bar: drops the most recently played round of the list as the database
 * has it. True when a round was removed.
 */
class RemoveLastDoppelkopfScoreUseCase @Inject constructor(
    private val scoreRepository: DoppelkopfScoresRepository,
)
{
    suspend operator fun invoke(gameId: UUID): Result<Boolean> =
        scoreRepository.deleteLast(gameId).map { it != null }
}
