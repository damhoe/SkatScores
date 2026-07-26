package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveLastScoreUseCase @Inject constructor(
    private val repository: SkatGamesRepository,
    private val refreshGameDataUseCase: RefreshAllUseCase
)
{
    suspend operator fun invoke(skatGame: SkatGame): Result<SkatGame>
    {
        return withContext(Dispatchers.IO) {
            skatGame.removeLastScore()
            runBlocking { refreshGameDataUseCase() }
            Result.success(skatGame)
        }
    }
}