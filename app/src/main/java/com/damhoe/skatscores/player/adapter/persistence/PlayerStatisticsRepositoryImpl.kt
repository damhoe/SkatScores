package com.damhoe.skatscores.player.adapter.persistence

import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import com.damhoe.skatscores.player.domain.PlayerStatistics
import java.util.UUID
import javax.inject.Inject

class PlayerStatisticsRepositoryImpl @Inject constructor(
    private val statisticsDao: PlayerStatisticsPersistenceAdapter,
) : PlayerStatisticsRepository
{
    override suspend fun getTotalGamesPlayed(playerId: UUID): Result<Int>
    {
        return statisticsDao.getGameCount(playerId)
    }

    override suspend fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics>
    {
        return statisticsDao.getPlayerStatistics(playerId)
    }
}