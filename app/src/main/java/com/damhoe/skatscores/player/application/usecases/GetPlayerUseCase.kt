package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayersRepository
import com.damhoe.skatscores.player.domain.Player
import java.util.UUID
import javax.inject.Inject

class GetPlayerUseCase @Inject constructor(
    private val repository: PlayersRepository,
)
{
    operator fun invoke(id: UUID): Result<Player?>
    {
        return repository.get(id)
    }
}

