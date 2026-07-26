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

                // A seat keeps its participant id even when another person takes it over,
                // otherwise the scores already recorded lose their score board column.
                val updatedGame = skatGame.copy(
                    participants = SkatParticipants(
                        foreHand = command.participants.foreHand
                            .withId(skatGame.participants.foreHand.id),
                        middleHand = command.participants.middleHand
                            .withId(skatGame.participants.middleHand.id),
                        rearHand = command.participants.rearHand
                            .withId(skatGame.participants.rearHand.id),
                    )
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