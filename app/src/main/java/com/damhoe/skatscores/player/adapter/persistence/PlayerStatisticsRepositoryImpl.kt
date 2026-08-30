package com.damhoe.skatscores.player.adapter.persistence

import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import com.damhoe.skatscores.player.domain.ListCounts
import com.damhoe.skatscores.player.domain.PlayerStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class PlayerStatisticsRepositoryImpl @Inject constructor(
    private val statisticsDao: PlayerStatisticsPersistenceAdapter,
    databaseChanges: DatabaseChanges,
) : PlayerStatisticsRepository
{
    override val changes: Flow<Unit> = databaseChanges.revision.map { }

    override suspend fun getListCounts(playerId: UUID): Result<ListCounts> =
        statisticsDao.getListCounts(playerId)

    override suspend fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics> =
        statisticsDao.getPlayerStatistics(playerId)
}
