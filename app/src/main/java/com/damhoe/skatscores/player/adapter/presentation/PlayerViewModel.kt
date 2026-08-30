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
    /**
     * The registered players, held by the repository rather than by an observer here: screens
     * that only read [playerNames] or [loadPlayerById] never observe a list, and a LiveData
     * nobody observes has no value to read.
     */
    private val players: StateFlow<List<Player>> = playerUseCases.getAllPlayers()

    /**
     * Players with the lists each has taken part in, counted per game. The counts are fetched
     * per player rather than carried on [Player]: they are derived from the games table, not
     * stored on the profile - which is why this also has to be rebuilt whenever a list is
     * played, added or deleted, not only when the players themselves change.
     */
    val playerInfos: LiveData<List<PlayerInfo>> = combine(
        players,
        playerUseCases.statisticsChanges(),
    ) { players, _ ->
        val slots = avatarSlots(players)

        players.map { player ->
            PlayerInfo(
                playerId = player.id,
                name = player.name,
                listCounts = playerUseCases.getListCounts(player.id)
                    .getOrDefault(ListCounts()),
                avatarSlot = slots[player.id] ?: 0,
            )
        }
    }.flowOn(Dispatchers.IO).asLiveData()

    /**
     * A palette slot per player, dealt out in the order the profiles were created.
     *
     * Dealing rather than hashing is what keeps the colours apart: with a colour per player id
     * the first handful collided more often than not. In creation order the first six players
     * are six different colours, and a new profile is always dealt last, so nobody who already
     * has a colour loses it when somebody joins.
     *
     * Deleting a profile does shift the players created after it along by one. That is the
     * price of not storing the colour on the row, and it only happens on an action that is
     * already confirmed and rare.
     */
    private fun avatarSlots(players: List<Player>): Map<UUID, Int> = players
        // Created within the same millisecond is unlikely but must still be an order, or two
        // players could swap colours between reads.
        .sortedWith(compareBy({ it.createdAt }, { it.id.toString() }))
        .withIndex()
        .associate { (slot, player) -> player.id to slot }

    /** The same slot the list gives this player, so their page shows the colour they have. */
    fun avatarSlotOf(playerId: UUID): Int = avatarSlots(players.value)[playerId] ?: 0

    fun playerNames(): List<String> = players.value.map { it.name.value }

    private val _playerDetails = MutableLiveData<Player?>()
    val playerDetails: LiveData<Player?> = _playerDetails

    private val _playerStatistics = MutableLiveData<PlayerStatistics?>()
    val playerStatistics: LiveData<PlayerStatistics?> = _playerStatistics

    fun loadPlayerById(playerId: UUID)
    {
        _playerDetails.postValue(players.value.firstOrNull { it.id == playerId })

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
        return players.value.any {
            it.name.value.equals(name.trim(), ignoreCase = true)
        }
    }
}