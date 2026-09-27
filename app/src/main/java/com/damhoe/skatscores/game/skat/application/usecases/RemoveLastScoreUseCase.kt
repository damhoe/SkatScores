package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import java.util.UUID
import javax.inject.Inject

/** The only way a round is removed. True when there was a round to remove. */
class RemoveLastScoreUseCase @Inject constructor(
    private val scoreRepository: SkatScoresRepository,
)
{
    suspend operator fun invoke(gameId: UUID): Result<Boolean> =
        scoreRepository.deleteLast(gameId).map { it != null }
}
