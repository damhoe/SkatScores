package com.damhoe.skatscores.player.application.repositories

import com.damhoe.skatscores.player.domain.ListCounts
import com.damhoe.skatscores.player.domain.PlayerStatistics
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface PlayerStatisticsRepository
{
    /** Players without a list are absent. */
    suspend fun getAllListCounts(): Result<Map<UUID, ListCounts>>

    suspend fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics>

    /** Emits immediately and whenever a list, round or player changes. */
    val changes: Flow<Unit>
}
