package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayersRepository
import javax.inject.Inject

class RefreshAllPlayersUseCase @Inject constructor(
    private val repository: PlayersRepository,
)
{
    suspend operator fun invoke()
    {
        repository.refreshAll()
    }
}