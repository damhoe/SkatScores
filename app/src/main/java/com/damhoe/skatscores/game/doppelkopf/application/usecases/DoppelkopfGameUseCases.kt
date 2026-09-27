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

/** Deliberately no delete-by-id: only the newest round can be removed. */
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
    /** Null starts with placeholder guests. */
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
 * A seat change is a correction: the seat keeps its participant id, so its recorded rounds are
 * credited to the new name.
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

/** The only way a round is removed. True when there was a round to remove. */
class RemoveLastDoppelkopfScoreUseCase @Inject constructor(
    private val scoreRepository: DoppelkopfScoresRepository,
)
{
    suspend operator fun invoke(gameId: UUID): Result<Boolean> =
        scoreRepository.deleteLast(gameId).map { it != null }
}
