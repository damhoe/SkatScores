package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import com.damhoe.skatscores.player.domain.PlayerStatistics
import java.util.UUID
import javax.inject.Inject

class GetPlayerStatisticsUseCase @Inject constructor(
    private val repository: PlayerStatisticsRepository,
)
{
    suspend operator fun invoke(playerId: UUID): Result<PlayerStatistics>
    {
        return repository.getPlayerStatistics(playerId)
    }
}