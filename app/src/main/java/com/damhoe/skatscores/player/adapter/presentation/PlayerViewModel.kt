package com.damhoe.skatscores.player.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.player.application.usecases.PlayerUseCases
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import com.damhoe.skatscores.player.domain.PlayerStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject internal constructor(
    private val playerUseCases: PlayerUseCases,
) : ViewModel()
{
    val players: LiveData<List<Player>> = playerUseCases.getAllPlayers().asLiveData()

    /**
     * Players with the number of lists each has taken part in. The count is fetched per player
     * rather than carried on [Player]: it is derived from the games table, not stored on the
     * profile.
     */
    val playerInfos: LiveData<List<PlayerInfo>> = players.switchMap { players ->
        liveData {
            emit(players.map { PlayerInfo(it.id, it.name, totalGamesPlayed = 0) })
            emit(players.map { player ->
                PlayerInfo(
                    playerId = player.id,
                    name = player.name,
                    totalGamesPlayed = playerUseCases.getTotalGamesPlayed(player.id)
                        .getOrDefault(0),
                )
            })
        }
    }

    fun playerNames(): List<String> = players.value.orEmpty().map { it.name.value }

    private val _playerDetails = MutableLiveData<Player?>()
    val playerDetails: LiveData<Player?> = _playerDetails

    private val _playerStatistics = MutableLiveData<PlayerStatistics?>()
    val playerStatistics: LiveData<PlayerStatistics?> = _playerStatistics

    fun loadPlayerById(playerId: UUID)
    {
        val allPlayers = players.value
        val player = allPlayers?.firstOrNull { it.id == playerId }
        _playerDetails.postValue(player)

        viewModelScope.launch {
            playerUseCases.getStatistics(playerId).onSuccess {
                _playerStatistics.postValue(it)
            }
        }
    }

    fun addPlayer(name: PlayerName)
    {
        val player = Player(
            id = UUID.randomUUID(),
            name = name
        )

        viewModelScope.launch {
            playerUseCases.addPlayer(player)
        }
    }

    fun removePlayer(player: Player)
    {
        viewModelScope.launch {
            if (_playerDetails.value?.id == player.id)
            {
                _playerDetails.postValue(null)
            }
            playerUseCases.deletePlayer(player.id)
        }
    }

    fun updatePlayer(player: Player)
    {
        viewModelScope.launch {
            playerUseCases.updatePlayer(player)
                .onSuccess {
                    if (_playerDetails.value?.id == player.id)
                    {
                        _playerDetails.postValue(player)
                    }
                }
        }
    }

    fun isPlayerNameTaken(name: String): Boolean
    {
        val currentPlayers = players.value ?: emptyList()
        return currentPlayers.any {
            it.name.value.equals(name.trim(), ignoreCase = true)
        }
    }
}