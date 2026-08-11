package com.damhoe.skatscores.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.usecases.CreateSkatGameCommand
import com.damhoe.skatscores.game.skat.application.usecases.CreateSkatGameUseCase
import com.damhoe.skatscores.game.skat.domain.SkatGameDefaults
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * A new list while it is being set up.
 *
 * Seats are deliberately absent: a new list starts with placeholder players and they are
 * chosen in the game view.
 */
data class NewListDraft(
    val title: String = "",
    val roundCount: SkatRoundCount = SkatGameDefaults.RoundCount,
    val scoringMode: SkatScoringMode = SkatScoringMode.CLASSIC,
)

@HiltViewModel
class NewListViewModel @Inject constructor(
    private val createSkatGameUseCase: CreateSkatGameUseCase,
) : ViewModel()
{
    private val _draft = MutableLiveData(NewListDraft())
    val draft: LiveData<NewListDraft> = _draft

    private val _titleError = MutableLiveData(false)
    val titleError: LiveData<Boolean> = _titleError

    private val _canStart = MutableLiveData(false)
    val canStart: LiveData<Boolean> = _canStart

    private val _navigateToGame = MutableLiveData<Event<UUID>>()
    val navigateToGame: LiveData<Event<UUID>> = _navigateToGame

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    val roundCountOptions: List<SkatRoundCount> =
        SkatRoundCount.ALLOWED_VALUES.map { SkatRoundCount(it) }

    fun setTitle(title: String) = edit { it.copy(title = title) }

    fun setRoundCount(roundCount: SkatRoundCount) = edit { it.copy(roundCount = roundCount) }

    fun setScoringMode(scoringMode: SkatScoringMode) = edit { it.copy(scoringMode = scoringMode) }

    /** Starts from a suggested name and round count, usually taken from the last list. */
    fun prefill(title: String, roundCount: SkatRoundCount?)
    {
        edit { draft ->
            draft.copy(title = title, roundCount = roundCount ?: draft.roundCount)
        }
    }

    private fun edit(transform: (NewListDraft) -> NewListDraft)
    {
        val updated = transform(_draft.value ?: NewListDraft())
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
            createSkatGameUseCase(
                CreateSkatGameCommand(
                    settings = SkatSettings(
                        roundCount = draft.roundCount,
                        scoringMode = draft.scoringMode,
                    ),
                    title = title,
                    playerCount = SkatGameDefaults.PlayerCount,
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
