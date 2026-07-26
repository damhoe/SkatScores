package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import javax.inject.Inject

class RefreshAllUseCase @Inject constructor(
    private val skatGameRepository: SkatGamesRepository
)
{
    suspend operator fun invoke()
    {
        return skatGameRepository.refreshAll()
    }
}