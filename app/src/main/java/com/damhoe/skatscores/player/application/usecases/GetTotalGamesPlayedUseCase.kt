package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import java.util.UUID
import javax.inject.Inject

class GetTotalGamesPlayedUseCase @Inject constructor(
    private val repository: PlayerStatisticsRepository,
)
{
    suspend operator fun invoke(playerId: UUID): Result<Int>
    {
        return repository.getTotalGamesPlayed(playerId)
    }
}