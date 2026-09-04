package com.damhoe.skatscores.player.application.usecases

import javax.inject.Inject

data class PlayerUseCases @Inject constructor(
    val getAllPlayers: GetAllPlayersUseCase,
    val getPlayer: GetPlayerUseCase,
    val addPlayer: AddPlayerUseCase,
    val updatePlayer: UpdatePlayerUseCase,
    val deletePlayer: DeletePlayerUseCase,
    val refreshAllPlayers: RefreshAllPlayersUseCase,
    val getStatistics: GetPlayerStatisticsUseCase,
    val getListCounts: GetListCountsUseCase,
    val statisticsChanges: GetStatisticsChangesUseCase,
)