package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.doppelkopf.application.usecases.CreateDoppelkopfGameCommand
import com.damhoe.skatscores.game.doppelkopf.application.usecases.DoppelkopfGameUseCases
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGameDefaults
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfRoundCount
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfScoringMode
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSettings
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * A new Doppelkopf list while it is being set up.
 *
 * Seats are deliberately absent, as for a Skat list: a new list starts with placeholder players
 * and they are chosen in the game view.
 */
data class NewDoppelkopfListDraft(
    val title: String = "",
    val roundCount: DoppelkopfRoundCount = DoppelkopfGameDefaults.RoundCount,
    val scoringMode: DoppelkopfScoringMode = DoppelkopfGameDefaults.ScoringMode,
)

@HiltViewModel
class NewDoppelkopfListViewModel @Inject constructor(
    private val useCases: DoppelkopfGameUseCases,
) : ViewModel()
{
    private val _draft = MutableLiveData(NewDoppelkopfListDraft())
    val draft: LiveData<NewDoppelkopfListDraft> = _draft

    private val _titleError = MutableLiveData(false)
    val titleError: LiveData<Boolean> = _titleError

    private val _canStart = MutableLiveData(false)
    val canStart: LiveData<Boolean> = _canStart

    private val _navigateToGame = MutableLiveData<Event<UUID>>()
    val navigateToGame: LiveData<Event<UUID>> = _navigateToGame

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    val roundCountOptions: List<DoppelkopfRoundCount> =
        DoppelkopfRoundCount.ALLOWED_VALUES.map { DoppelkopfRoundCount(it) }

    fun setTitle(title: String) = edit { it.copy(title = title) }

    fun setRoundCount(roundCount: DoppelkopfRoundCount) = edit { it.copy(roundCount = roundCount) }

    fun setScoringMode(scoringMode: DoppelkopfScoringMode) =
        edit { it.copy(scoringMode = scoringMode) }

    /** Starts from a suggested name and round count, usually taken from the last list. */
    fun prefill(title: String, roundCount: DoppelkopfRoundCount?) = edit { draft ->
        draft.copy(title = title, roundCount = roundCount ?: draft.roundCount)
    }

    private fun edit(transform: (NewDoppelkopfListDraft) -> NewDoppelkopfListDraft)
    {
        val updated = transform(_draft.value ?: NewDoppelkopfListDraft())
        val titleIsValid = Title.create(updated.title.trim()).isSuccess

        _draft.value = updated
        _titleError.value = !titleIsValid
        _canStart.value = titleIsValid
    }

    fun start()
    {
        val draft = _draft.value ?: return
        val title = Title.create(draft.title.trim()).getOrNull() ?: return

        viewModelScope.launch {
            useCases.createGame(
                CreateDoppelkopfGameCommand(
                    settings = DoppelkopfSettings(
                        roundCount = draft.roundCount,
                        scoringMode = draft.scoringMode,
                    ),
                    title = title,
                    // No participants: the use case seats placeholder guests.
                )
            ).fold(
                onSuccess = { game -> _navigateToGame.postValue(Event(game.id)) },
                onFailure = {
                    _errorMessage.postValue(Event(it.message ?: "Could not create list"))
                }
            )
        }
    }
}
