package com.damhoe.skatscores.game.skat.adapter.presentation

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** Observes the game, so writes from any sheet arrive without a refresh. */
@HiltViewModel
class SharedSkatGameViewModel @Inject constructor(
    private val skatGameUseCases: SkatGameUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val _skatGame = MutableLiveData<SkatGame>()
    val skatGame: LiveData<SkatGame> = _skatGame

    val skatParticipants: LiveData<SkatParticipants> = _skatGame.map { it.participants }

    private val _navigateUpEvent = MutableLiveData<Event<Unit>>()
    val navigateUpEvent: LiveData<Event<Unit>> = _navigateUpEvent

    private val _isUndoing = MutableLiveData(false)
    val isUndoing: LiveData<Boolean> = _isUndoing

    /** An undo finishes faster than a double tap, so a cooldown is needed as well. */
    private var lastUndoAt = 0L

    private val _roundRemovedEvent = MutableLiveData<Event<Unit>>()
    val roundRemovedEvent: LiveData<Event<Unit>> = _roundRemovedEvent

    private val _isBreakdownExpanded = MutableLiveData(false)
    val isBreakdownExpanded: LiveData<Boolean> = _isBreakdownExpanded

    val skatGameId: UUID? = savedStateHandle.get<String>("gameId")
        ?.let { UUID.fromString(it) }

    init
    {
        if (skatGameId == null) _navigateUpEvent.value = Event(Unit) else observe(skatGameId)
    }

    fun toggleBreakdown()
    {
        _isBreakdownExpanded.value = _isBreakdownExpanded.value != true
    }

    /** A later null means the settings sheet deleted the list; that sheet navigates away. */
    private fun observe(skatGameId: UUID) = viewModelScope.launch {
        var hasLoaded = false

        skatGameUseCases.observeSkatGame(skatGameId).collect { skatGame ->
            when
            {
                skatGame != null ->
                {
                    hasLoaded = true
                    _skatGame.value = skatGame
                }

                !hasLoaded -> _navigateUpEvent.value = Event(Unit)
            }
        }
    }

    fun removeLastScore()
    {
        val gameId = skatGameId ?: return
        if (_isUndoing.value == true) return
        if (SystemClock.elapsedRealtime() - lastUndoAt < UNDO_COOLDOWN_MS) return

        _isUndoing.value = true
        viewModelScope.launch {
            skatGameUseCases.removeLastScore(gameId)
                .onSuccess { removed -> if (removed) _roundRemovedEvent.value = Event(Unit) }
            lastUndoAt = SystemClock.elapsedRealtime()
            _isUndoing.value = false
        }
    }

    private companion object
    {
        const val UNDO_COOLDOWN_MS = 700L
    }
}
