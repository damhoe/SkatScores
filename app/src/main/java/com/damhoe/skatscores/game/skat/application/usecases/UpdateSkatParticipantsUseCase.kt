package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import jakarta.inject.Inject
import java.util.UUID

data class UpdateSkatParticipantsCommand(
    val id: UUID,
    val participants: SkatParticipants
)


class UpdateSkatParticipantsUseCase @Inject constructor(
    private val skatGameRepository: SkatGamesRepository,
    private val getGameDetailsUseCase: GetSkatGameUseCase,
)
{
    suspend operator fun invoke(command: UpdateSkatParticipantsCommand): Result<SkatGame>
    {
        return getGameDetailsUseCase(command.id).fold(
            onSuccess = { skatGame ->
                if (skatGame == null) {
                    return Result.failure(Exception("Game not found with ID: ${command.id}"))
                }

                val updatedGame = skatGame.copy(participants = command.participants)

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