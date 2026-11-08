package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import android.os.SystemClock
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

/** Shared with the chart screen. Observes the game, so writes from any sheet arrive by themselves. */
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

    private val _isUndoing = MutableLiveData(false)
    val isUndoing: LiveData<Boolean> = _isUndoing

    /** An undo finishes faster than a double tap, so a cooldown is needed as well. */
    private var lastUndoAt = 0L

    private val _roundRemovedEvent = MutableLiveData<Event<Unit>>()
    val roundRemovedEvent: LiveData<Event<Unit>> = _roundRemovedEvent

    val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    init
    {
        if (gameId == null) _navigateUpEvent.value = Event(Unit) else observe(gameId)
    }

    /** A later null means the settings sheet deleted the list; that sheet navigates away. */
    private fun observe(gameId: UUID) = viewModelScope.launch {
        var hasLoaded = false

        useCases.observeGame(gameId).collect { game ->
            when
            {
                game != null ->
                {
                    hasLoaded = true
                    _game.value = game
                }

                !hasLoaded -> _navigateUpEvent.value = Event(Unit)
            }
        }
    }

    fun removeLastScore()
    {
        val gameId = gameId ?: return
        if (_isUndoing.value == true) return
        if (SystemClock.elapsedRealtime() - lastUndoAt < UNDO_COOLDOWN_MS) return

        _isUndoing.value = true
        viewModelScope.launch {
            useCases.removeLastScore(gameId)
                .onSuccess { removed -> if (removed) _roundRemovedEvent.value = Event(Unit) }
            lastUndoAt = SystemClock.elapsedRealtime()
            _isUndoing.value = false
        }
    }

    companion object
    {
        private const val UNDO_COOLDOWN_MS = 700L

        const val ARG_GAME_ID = "gameId"
    }
}
