package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import javax.inject.Inject

class AddScoreToSkatGameUseCase @Inject constructor(
    private val scoreRepository: SkatScoresRepository
)
{
    /**
     * Appends [score] as the newest round. Where it lands is decided by the database at the
     * moment of writing, not by how many rounds [skatGame] held when it was read.
     */
    suspend operator fun invoke(
        skatGame: SkatGame,
        score: SkatScore
    ): Result<SkatGame> = scoreRepository
        .insert(score = score, gameId = skatGame.id)
        .map { skatGame.addScore(score).updatedSkatGame }
}
