package com.damhoe.skatscores.game.skat.adapter.presentation

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

/**
 * Holds the game shown by [SkatGameFragment].
 *
 * The game is observed, not loaded: every write to it - a round saved in the round sheet, the
 * seats or settings changed in theirs, an undo here - arrives on its own. This used to be a
 * copy refreshed when a sheet reported back, and undo acted on that copy, so tapping it right
 * after a save could take back the round before the one just saved.
 */
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

    /** True while an undo is on its way to the database; the button waits for it. */
    private val _isUndoing = MutableLiveData(false)
    val isUndoing: LiveData<Boolean> = _isUndoing

    /** Fires once a round has actually been taken back. */
    private val _roundRemovedEvent = MutableLiveData<Event<Unit>>()
    val roundRemovedEvent: LiveData<Event<Unit>> = _roundRemovedEvent

    /**
     * Whether the score board shows the tournament breakdown. Collapsed whenever a list is
     * opened - the board is pinned above the round log, so the extra height is opt-in - but
     * kept here rather than in the fragment so it survives a rotation.
     */
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

    /**
     * A list that cannot be read when the screen opens sends the user back. One that goes away
     * later was deleted from the settings sheet, which leaves the screen itself.
     */
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

    /**
     * Undo in the bottom bar: drops the round that was played last, and the only way a round
     * is ever removed. A round in the middle of the log can be edited from its row, but not
     * deleted. A second tap while the first is still on its way is ignored, so one tap takes
     * back one round.
     */
    fun removeLastScore()
    {
        val gameId = skatGameId ?: return
        if (_isUndoing.value == true) return

        _isUndoing.value = true
        viewModelScope.launch {
            skatGameUseCases.removeLastScore(gameId)
                .onSuccess { removed -> if (removed) _roundRemovedEvent.value = Event(Unit) }
            _isUndoing.value = false
        }
    }
}
