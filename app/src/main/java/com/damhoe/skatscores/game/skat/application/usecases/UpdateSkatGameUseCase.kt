package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import jakarta.inject.Inject
import java.util.UUID

data class UpdateSkatGameCommand(
    val id: UUID,
    val title: Title,
    val settings: SkatSettings
)

class UpdateSkatGameUseCase @Inject constructor(
    private val skatGameRepository: SkatGamesRepository,
    private val getGameDetailsUseCase: GetSkatGameUseCase,
)
{
    suspend operator fun invoke(command: UpdateSkatGameCommand): Result<SkatGame>
    {
        return getGameDetailsUseCase(command.id)
            .fold(
                onSuccess = { skatGame ->
                    if (skatGame == null)
                    {
                        return Result.failure(Exception("Game not found with ID: ${command.id}"))
                    }

                    val updatedGame = skatGame.copy(
                        title = command.title,
                        settings = command.settings
                    )

                    skatGameRepository.update(updatedGame).map {
                        updatedGame
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
    }
}