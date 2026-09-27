package com.damhoe.skatscores.player.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.player.application.usecases.PlayerUseCases
import com.damhoe.skatscores.player.domain.ListCounts
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import com.damhoe.skatscores.player.domain.PlayerStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject internal constructor(
    private val playerUseCases: PlayerUseCases,
) : ViewModel()
{
    /** A StateFlow, not LiveData: some screens read it without observing. */
    private val players: StateFlow<List<Player>> = playerUseCases.getAllPlayers()

    /** Rebuilt on list changes too, since the counts come from the lists. */
    val playerInfos: LiveData<List<PlayerInfo>> = combine(
        players,
        playerUseCases.statisticsChanges(),
    ) { players, _ ->
        val slots = avatarSlots(players)
        val counts = playerUseCases.getListCounts().getOrDefault(emptyMap())

        players.map { player ->
            PlayerInfo(
                playerId = player.id,
                name = player.name,
                listCounts = counts[player.id] ?: ListCounts(),
                avatarSlot = slots[player.id] ?: 0,
            )
        }
    }.flowOn(Dispatchers.IO).asLiveData()

    /**
     * Dealt in creation order, so the first six players get six different colours and a new
     * player never changes anyone else's. Deleting a player shifts later ones by one.
     */
    private fun avatarSlots(players: List<Player>): Map<UUID, Int> = players
        // The id breaks ties so the order is stable.
        .sortedWith(compareBy({ it.createdAt }, { it.id.toString() }))
        .withIndex()
        .associate { (slot, player) -> player.id to slot }

    private val loadedPlayerId = MutableStateFlow<UUID?>(null)

    /** Observed, because the player list may still be loading. */
    val avatarSlot: LiveData<Int> = combine(players, loadedPlayerId) { players, id ->
        id?.let { avatarSlots(players)[it] } ?: 0
    }.asLiveData()

    fun playerNames(): List<String> = players.value.map { it.name.value }

    private val _playerDetails = MutableLiveData<Player?>()
    val playerDetails: LiveData<Player?> = _playerDetails

    private val _playerStatistics = MutableLiveData<PlayerStatistics?>()
    val playerStatistics: LiveData<PlayerStatistics?> = _playerStatistics

    fun loadPlayerById(playerId: UUID)
    {
        loadedPlayerId.value = playerId

        viewModelScope.launch {
            val player = players.value.firstOrNull { it.id == playerId }
                ?: playerUseCases.getPlayer(playerId).getOrNull()

            _playerDetails.postValue(player)

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
}
