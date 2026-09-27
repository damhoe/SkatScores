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

/**
 * Holds the game shown by [DoppelkopfGameFragment], and shared with the chart screen through
 * the Doppelkopf navigation graph.
 *
 * The game is observed, not loaded: every write to it - from the round sheet, the settings or
 * seats sheet, or undo here - arrives on its own, so undo never acts on an outdated copy.
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

    /** True while an undo is on its way to the database; the button waits for it. */
    private val _isUndoing = MutableLiveData(false)
    val isUndoing: LiveData<Boolean> = _isUndoing

    /**
     * When the last undo finished. An undo is over in a few milliseconds, faster than a
     * double tap, so waiting for it alone let one accidental double tap take back two rounds.
     */
    private var lastUndoAt = 0L

    /** Fires once a round has actually been taken back. */
    private val _roundRemovedEvent = MutableLiveData<Event<Unit>>()
    val roundRemovedEvent: LiveData<Event<Unit>> = _roundRemovedEvent

    val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    init
    {
        if (gameId == null) _navigateUpEvent.value = Event(Unit) else observe(gameId)
    }

    /**
     * A list that cannot be read when the screen opens sends the user back. One that goes away
     * later was deleted from the settings sheet, which leaves the screen itself.
     */
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

    /**
     * Undo in the bottom bar: drops the round that was played last, and the only way a round
     * is ever removed. A second tap while the first is on its way is ignored.
     */
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
