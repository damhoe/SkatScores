package com.damhoe.skatscores.game.skat.adapter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.player.application.usecases.GetAllPlayersUseCase
import com.damhoe.skatscores.player.application.usecases.GetPlayerUseCase
import com.damhoe.skatscores.player.application.usecases.PlayerUseCases
import com.damhoe.skatscores.player.domain.Player
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class SelectPlayerViewModel(
    private val playerUseCases: PlayerUseCases,
) : ViewModel()
{
    val allPlayers: StateFlow<List<Player>> = playerUseCases.getAllPlayers()
}
