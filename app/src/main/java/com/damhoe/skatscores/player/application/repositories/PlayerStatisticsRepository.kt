package com.damhoe.skatscores.player.application.repositories

import com.damhoe.skatscores.player.domain.ListCounts
import com.damhoe.skatscores.player.domain.PlayerStatistics
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface PlayerStatisticsRepository
{
    /** Lists this player appears in, split by game. */
    suspend fun getListCounts(playerId: UUID): Result<ListCounts>
    suspend fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics>

    /**
     * Emits at once and again whenever a list or round these numbers are read from changes.
     * They are counted on demand rather than stored, so nothing else would tell a screen
     * showing them that they have moved on.
     */
    val changes: Flow<Unit>
}
