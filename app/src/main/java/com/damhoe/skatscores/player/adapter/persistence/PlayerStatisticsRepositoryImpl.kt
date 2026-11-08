package com.damhoe.skatscores.player.adapter.persistence

import com.damhoe.skatscores.persistence.DataTopic
import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import com.damhoe.skatscores.player.domain.ListCounts
import com.damhoe.skatscores.player.domain.PlayerStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class PlayerStatisticsRepositoryImpl @Inject constructor(
    private val statisticsDao: PlayerStatisticsPersistenceAdapter,
    databaseChanges: DatabaseChanges,
) : PlayerStatisticsRepository
{
    override val changes: Flow<Unit> =
        databaseChanges.changes(DataTopic.PLAYERS, DataTopic.SKAT, DataTopic.DOPPELKOPF)

    override suspend fun getAllListCounts(): Result<Map<UUID, ListCounts>> =
        withContext(Dispatchers.IO) { statisticsDao.getAllListCounts() }

    override suspend fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics> =
        withContext(Dispatchers.IO) { statisticsDao.getPlayerStatistics(playerId) }
}
