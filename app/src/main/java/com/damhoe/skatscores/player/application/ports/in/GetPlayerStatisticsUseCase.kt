package com.damhoe.skatscores.player.application.ports.`in`

import com.damhoe.skatscores.player.domain.PlayerStatistics

interface GetPlayerStatisticsUseCase {
    fun getPlayerStatistics(playerId: Long): PlayerStatistics
}