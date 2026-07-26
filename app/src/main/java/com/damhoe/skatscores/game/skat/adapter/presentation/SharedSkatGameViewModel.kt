package com.damhoe.skatscores.game.skat.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases
import com.damhoe.skatscores.game.skat.application.usecases.UpdateSkatGameCommand
import com.damhoe.skatscores.game.skat.application.usecases.UpdateSkatParticipantsCommand
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatPlayers
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.player.application.usecases.PlayerUseCases
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SharedSkatGameViewModel @Inject constructor(
    private val skatGameUseCases: SkatGameUseCases,
    private val playerUseCases: PlayerUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private var _skatGame = MutableLiveData<SkatGame>()
    val skatGame: LiveData<SkatGame> = _skatGame

    val skatGameTitle: LiveData<String> = _skatGame.map { it.title.value }
    val skatParticipants: LiveData<SkatParticipants> = _skatGame.map { it.participants }

    val allRegisteredPlayers: StateFlow<List<Player>> = playerUseCases.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _navigateUpEvent = MutableLiveData<Event<Unit>>()
    val navigateUpEvent: LiveData<Event<Unit>> = _navigateUpEvent

    val skatGameId = savedStateHandle.get<String>("gameId")
        ?.let { UUID.fromString(it) }
        ?.also { initialize(it) }
        ?: _navigateUpEvent.postValue(Event(Unit)).let { null }

    val totalPoints: LiveData<IntArray> =
        skatGame.map { it.calculateTotalPoints() }
    val winBonus: LiveData<IntArray> =
        skatGame.map { it.calculateWinBonus() }
    val lossOfOthersBonus: LiveData<IntArray> =
        skatGame.map { it.calculateLossOfOthersBonus() }

    val dealerPosition: LiveData<Int> = skatGame.map { it.dealerPosition }

    fun initialize(skatGameId: UUID)
    {
        viewModelScope.launch {
            skatGameUseCases.getSkatGame(skatGameId)
                .onSuccess { skatGame ->
                    if (skatGame == null)
                    {
                        _navigateUpEvent.postValue(Event(Unit))
                        return@launch
                    }
                    _skatGame.postValue(skatGame)
                }
                .onFailure {
                    _navigateUpEvent.postValue(Event(Unit))
                }
        }
    }

    fun refresh()
    {
        viewModelScope.launch {
            skatGameUseCases.getSkatGame(skatGameId!!)
                .onSuccess { skatGame ->
                    if (skatGame == null)
                    {
                        _navigateUpEvent.postValue(Event(Unit))
                        return@launch
                    }
                    _skatGame.postValue(skatGame)
                }
                .onFailure {
                    _navigateUpEvent.postValue(Event(Unit))
                }
        }
    }

    private suspend fun resolveSkatParticipants(gamePlayers: SkatPlayers): SkatParticipants
    {
        val forehandParticipant = resolveSkatParticipant(
            gamePlayers.forehand,
            "Player 1"
        )
        val middlehandParticipant = resolveSkatParticipant(
            gamePlayers.middlehand,
            "Player 2"
        )
        val rearhandParticipant = resolveSkatParticipant(
            gamePlayers.rearhand,
            "Player 3"
        )
        return SkatParticipants(
            foreHand = forehandParticipant,
            middleHand = middlehandParticipant,
            rearHand = rearhandParticipant
        )
    }

    private fun resolveSkatParticipant(
        playerId: UUID?, defaultGuestNameStr: String
    ): SkatParticipant
    {
        val safeDefaultPlayerName =
            PlayerName.create(defaultGuestNameStr).getOrElse { PlayerName("Guest") }
        return if (playerId != null)
        {
            playerUseCases.getPlayer(playerId)
                .fold(
                    onSuccess = { player: Player? ->
                        if (player != null)
                        {
                            SkatParticipant.Registered.from(player)
                        } else
                        {
                            val notFoundName = PlayerName.create("$defaultGuestNameStr (Not found)")
                                .getOrElse { PlayerName("Guest (Not found)") }
                            SkatParticipant.Guest(notFoundName)
                        }
                    },
                    onFailure = { _ ->
                        val errorName = PlayerName.create("$defaultGuestNameStr (Error)")
                            .getOrElse { PlayerName("Guest (Error)") }
                        SkatParticipant.Guest(errorName)
                    }
                )
        } else
        {
            SkatParticipant.Guest(safeDefaultPlayerName)
        }
    }

    fun updateParticipants(
        forehand: SkatParticipant,
        middlehand: SkatParticipant,
        rearhand: SkatParticipant,
    )
    {
        viewModelScope.launch {
            val currentGame = _skatGame.value ?: return@launch

            skatGameUseCases.updateSkatParticipants(
                UpdateSkatParticipantsCommand(
                    currentGame.id,
                    SkatParticipants(forehand, middlehand, rearhand)
                )
            ).onSuccess { updatedGame -> _skatGame.postValue(updatedGame) }
        }
    }

    fun updateGameSettings(
        title: Title,
        settings: SkatSettings
    )
    {
        viewModelScope.launch {
            val currentGame = _skatGame.value ?: return@launch
            skatGameUseCases.updateSkatGame(
                UpdateSkatGameCommand(
                    currentGame.id,
                    title,
                    settings,
                )
            ).onSuccess { updatedGame -> _skatGame.postValue(updatedGame) }
        }
    }

    fun addScore(score: SkatScore)
    {
        viewModelScope.launch {
            val currentGame = _skatGame.value
            if (currentGame != null)
            {
                skatGameUseCases.addScoreToSkatGame(
                    skatGame = currentGame,
                    score = score
                )
                    .onSuccess { updatedGame ->
                        _skatGame.postValue(updatedGame)
                    }
                    .onFailure {
                        // Handle failure
                    }
            } else
            {
                // Handle game being null
            }
        }
    }
}