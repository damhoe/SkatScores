package com.damhoe.skatscores.player.adapter.out

import com.damhoe.skatscores.player.application.ports.out.GetPlayerStatisticsPort
import javax.inject.Inject

class PlayerStatisticsRepository @Inject constructor(
    private val adapter: PlayerStatisticsPersistenceAdapter
) : GetPlayerStatisticsPort{

    override fun getListCount(playerId: Long) = adapter.getListCount(playerId)

    override fun getGamesCount(playerId: Long) = adapter.getRoundCount(playerId)

    override fun getSoloCount(playerId: Long) = adapter.getSoloCount(playerId)

    override fun getSoloWinsCount(playerId: Long) = adapter.getSoloWinsCount(playerId)

    override fun getAgainstWinsCount(playerId: Long) = adapter.getAgainstWinsCount(playerId)
}