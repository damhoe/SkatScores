package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import javax.inject.Inject

/** Replaces one round of a game with an edited version, keeping its position in the list. */
class UpdateScoreUseCase @Inject constructor(
    private val scoreRepository: SkatScoresRepository
)
{
    suspend operator fun invoke(
        skatGame: SkatGame,
        score: SkatScore
    ): Result<SkatGame> = scoreRepository
        .update(score = score, gameId = skatGame.id)
        .map { skatGame.updateScore(score) }
}
