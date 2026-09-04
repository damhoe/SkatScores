package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayersRepository
import com.damhoe.skatscores.player.domain.Player
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetAllPlayersUseCase @Inject constructor(
    private val repository: PlayersRepository,
)
{
    operator fun invoke(): StateFlow<List<Player>>
    {
        return repository.allPlayers
    }
}