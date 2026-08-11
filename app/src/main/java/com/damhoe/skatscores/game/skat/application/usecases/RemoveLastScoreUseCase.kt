package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.domain.SkatGame
import javax.inject.Inject

/** Undo for the bottom bar: drops the most recently played round. */
class RemoveLastScoreUseCase @Inject constructor(
    private val deleteScore: DeleteScoreUseCase,
)
{
    suspend operator fun invoke(skatGame: SkatGame): Result<SkatGame>
    {
        val lastScore = skatGame.scores.lastOrNull()
            ?: return Result.success(skatGame)

        return deleteScore(skatGame, lastScore.id)
    }
}
