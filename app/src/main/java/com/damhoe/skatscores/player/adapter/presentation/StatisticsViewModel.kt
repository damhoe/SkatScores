package com.damhoe.skatscores.player.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.player.application.usecases.PlayerUseCases
import com.damhoe.skatscores.player.domain.PlayerStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val playerUseCases: PlayerUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val playerId: UUID? = savedStateHandle.get<String>(ARG_PLAYER_ID)
        ?.let { UUID.fromString(it) }

    private val _playerName = MutableLiveData<String>()
    val playerName: LiveData<String> = _playerName

    private val _statistics = MutableLiveData<PlayerStatistics?>()
    val statistics: LiveData<PlayerStatistics?> = _statistics

    init
    {
        playerId?.let { load(it) }
    }

    private fun load(playerId: UUID) = viewModelScope.launch {
        playerUseCases.getPlayer(playerId)
            .onSuccess { player -> player?.let { _playerName.postValue(it.name.value) } }

        playerUseCases.getStatistics(playerId)
            .onSuccess { _statistics.postValue(it) }
            .onFailure { _statistics.postValue(null) }
    }

    companion object
    {
        const val ARG_PLAYER_ID = "playerId"
    }
}
