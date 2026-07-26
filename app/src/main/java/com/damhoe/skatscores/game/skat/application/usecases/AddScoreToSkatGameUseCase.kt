package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.application.repository.SkatScoresRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import javax.inject.Inject

class AddScoreToSkatGameUseCase @Inject constructor(
    private val scoreRepository: SkatScoresRepository
)
{
    suspend operator fun invoke(
        skatGame: SkatGame,
        score: SkatScore
    ): Result<SkatGame>
    {
        val result = skatGame.addScore(score)

        return scoreRepository.insert(
            score = result.score,
            gameId = skatGame.id,
            round = result.round,
        ).map {
            result.updatedSkatGame
        }
    }
}