package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayersRepository
import com.damhoe.skatscores.player.domain.Player
import javax.inject.Inject

class AddPlayerUseCase @Inject constructor(
    private val repository: PlayersRepository,
)
{
    suspend operator fun invoke(player: Player): Result<Unit>
    {
        return repository.insert(player)
    }
}