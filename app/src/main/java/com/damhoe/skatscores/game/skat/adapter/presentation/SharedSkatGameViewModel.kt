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
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Holds the game shown by [SkatGameFragment].
 *
 * Editing surfaces (round sheet, settings sheet, players sheet) own their own saves and report
 * back with a fragment result, so this only loads, refreshes and applies the two actions that
 * live on the game screen itself.
 */
@HiltViewModel
class SharedSkatGameViewModel @Inject constructor(
    private val skatGameUseCases: SkatGameUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private var _skatGame = MutableLiveData<SkatGame>()
    val skatGame: LiveData<SkatGame> = _skatGame

    val skatParticipants: LiveData<SkatParticipants> = _skatGame.map { it.participants }

    private val _navigateUpEvent = MutableLiveData<Event<Unit>>()
    val navigateUpEvent: LiveData<Event<Unit>> = _navigateUpEvent

    /**
     * Whether the score board shows the tournament breakdown. Collapsed whenever a list is
     * opened - the board is pinned above the round log, so the extra height is opt-in - but
     * kept here rather than in the fragment so it survives a rotation.
     */
    private val _isBreakdownExpanded = MutableLiveData(false)
    val isBreakdownExpanded: LiveData<Boolean> = _isBreakdownExpanded

    val skatGameId = savedStateHandle.get<String>("gameId")
        ?.let { UUID.fromString(it) }
        ?.also { initialize(it) }
        ?: _navigateUpEvent.postValue(Event(Unit)).let { null }

    fun initialize(skatGameId: UUID) = load(skatGameId)

    fun toggleBreakdown()
    {
        _isBreakdownExpanded.value = _isBreakdownExpanded.value != true
    }

    fun refresh()
    {
        skatGameId?.let { load(it) }
    }

    private fun load(skatGameId: UUID)
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

    fun addScore(score: SkatScore)
    {
        viewModelScope.launch {
            val currentGame = _skatGame.value ?: return@launch
            skatGameUseCases.addScoreToSkatGame(skatGame = currentGame, score = score)
                .onSuccess { updatedGame -> _skatGame.postValue(updatedGame) }
        }
    }

    /**
     * Undo in the bottom bar: drops the round that was played last, and the only way a round
     * is ever removed. There is deliberately no delete-by-id here - a round in the middle of
     * the log can be edited from its row, but not deleted, so the numbering cannot develop
     * holes. RemoveLastScoreUseCase is what reaches DeleteScoreUseCase, and it only ever
     * hands it the last score.
     */
    fun removeLastScore()
    {
        viewModelScope.launch {
            val currentGame = _skatGame.value ?: return@launch
            skatGameUseCases.removeLastScore(currentGame)
                .onSuccess { updatedGame -> _skatGame.postValue(updatedGame) }
        }
    }
}
