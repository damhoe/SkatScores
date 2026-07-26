package com.damhoe.skatscores.player.application.repositories

import com.damhoe.skatscores.player.domain.PlayerStatistics
import java.util.UUID

interface PlayerStatisticsRepository {
    suspend fun getTotalGamesPlayed(playerId: UUID): Result<Int>
    suspend fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics>
}