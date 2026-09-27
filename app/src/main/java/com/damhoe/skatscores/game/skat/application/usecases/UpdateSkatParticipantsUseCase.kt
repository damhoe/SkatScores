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
 * Changes who sits at the seats of a running list.
 *
 * A change is a correction: a seat keeps its participant id whoever takes it over, so the
 * rounds already recorded stay in that column and are credited to the name now on it - which
 * is right for filling in "Player 1" or fixing a mix-up. Swapping two people moves their
 * rounds with the names, for the same reason.
 */
class UpdateSkatParticipantsUseCase @Inject constructor(
    private val skatGameRepository: SkatGamesRepository,
)
{
    suspend operator fun invoke(command: UpdateSkatParticipantsCommand): Result<Unit> =
        skatGameRepository.updateSeats(command.id, command.participants)
}
