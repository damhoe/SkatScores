package com.damhoe.skatscores.player.adapter.`in`.ui

import androidx.lifecycle.ViewModel
import com.damhoe.skatscores.player.application.ports.`in`.GetPlayerStatisticsUseCase
import com.damhoe.skatscores.player.domain.PlayerStatistics

class PlayerStatisticsViewModel(
    private val getPlayerStatisticsUseCase: GetPlayerStatisticsUseCase
) : ViewModel() {

    fun loadPlayerStatistics(playerId: Long): PlayerStatistics {
        return getPlayerStatisticsUseCase.getPlayerStatistics(playerId)
    }
}