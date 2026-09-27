package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Undo for the bottom bar: drops the most recently played round, and the only way a round is
 * ever removed. It works on the list as the database has it rather than on a copy a screen
 * holds, so a round saved a moment ago is the one taken back. True when a round was removed.
 */
class RemoveLastScoreUseCase @Inject constructor(
    private val scoreRepository: SkatScoresRepository,
)
{
    suspend operator fun invoke(gameId: UUID): Result<Boolean> =
        scoreRepository.deleteLast(gameId).map { it != null }
}
