package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.doppelkopf.application.usecases.DoppelkopfGameUseCases
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Holds the game shown by [DoppelkopfGameFragment], and shared with the chart screen through
 * the Doppelkopf navigation graph.
 *
 * Editing surfaces (round sheet, settings sheet) own their own saves and report back with a
 * fragment result, so this only loads, refreshes and applies the one action that lives on the
 * game screen itself.
 */
@HiltViewModel
class SharedDoppelkopfGameViewModel @Inject constructor(
    private val useCases: DoppelkopfGameUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val _game = MutableLiveData<DoppelkopfGame>()
    val game: LiveData<DoppelkopfGame> = _game

    val participants: LiveData<DoppelkopfParticipants> = _game.map { it.participants }

    private val _navigateUpEvent = MutableLiveData<Event<Unit>>()
    val navigateUpEvent: LiveData<Event<Unit>> = _navigateUpEvent

    val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }
        ?.also { load(it) }
        ?: _navigateUpEvent.postValue(Event(Unit)).let { null }

    fun refresh()
    {
        gameId?.let { load(it) }
    }

    private fun load(gameId: UUID)
    {
        viewModelScope.launch {
            useCases.getGame(gameId)
                .onSuccess { game ->
                    if (game == null)
                    {
                        _navigateUpEvent.postValue(Event(Unit))
                        return@launch
                    }
                    _game.postValue(game)
                }
                .onFailure { _navigateUpEvent.postValue(Event(Unit)) }
        }
    }

    /**
     * Undo in the bottom bar: drops the round that was played last, and the only way a round
     * is ever removed. A round in the middle of the log can be edited from its row, but not
     * deleted, so the numbering cannot develop holes.
     */
    fun removeLastScore()
    {
        viewModelScope.launch {
            val currentGame = _game.value ?: return@launch
            useCases.removeLastScore(currentGame)
                .onSuccess { updated -> _game.postValue(updated) }
        }
    }

    companion object
    {
        const val ARG_GAME_ID = "gameId"
    }
}
