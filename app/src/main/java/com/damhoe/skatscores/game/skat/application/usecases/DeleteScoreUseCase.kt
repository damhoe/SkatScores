package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import java.util.UUID
import javax.inject.Inject

/** Removes one round from a game and closes the gap in the round numbering. */
class DeleteScoreUseCase @Inject constructor(
    private val scoreRepository: SkatScoresRepository
)
{
    suspend operator fun invoke(
        skatGame: SkatGame,
        scoreId: UUID
    ): Result<SkatGame> = scoreRepository
        .delete(id = scoreId, gameId = skatGame.id)
        .map { skatGame.removeScore(scoreId) }
}
