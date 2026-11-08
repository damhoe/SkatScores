package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import java.util.UUID
import javax.inject.Inject

data class UpdateSkatParticipantsCommand(
    val id: UUID,
    val participants: SkatParticipants
)

/**
 * A seat change is a correction: the seat keeps its participant id, so its recorded rounds are
 * credited to the new name.
 */
class UpdateSkatParticipantsUseCase @Inject constructor(
    private val skatGameRepository: SkatGamesRepository,
)
{
    suspend operator fun invoke(command: UpdateSkatParticipantsCommand): Result<Unit> =
        skatGameRepository.updateSeats(command.id, command.participants)
}
