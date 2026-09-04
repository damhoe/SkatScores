package com.damhoe.skatscores.game.skat.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases
import com.damhoe.skatscores.game.skat.application.usecases.UpdateSkatGameCommand
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class GameSettingsDraft(
    val title: String = "",
    val roundCount: SkatRoundCount? = null,
    val scoringMode: SkatScoringMode = SkatScoringMode.CLASSIC,
    /** Rounds already played; the list cannot be shortened below this. */
    val roundsPlayed: Int = 0,
)

/**
 * Everything about a running list that is not a round and not who is playing it: the name, the
 * round count and the scoring mode. The seats are their own screen, [PlayerSeatsViewModel],
 * because they are the thing that gets edited mid-evening.
 */
@HiltViewModel
class GameSettingsViewModel @Inject constructor(
    private val skatGameUseCases: SkatGameUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    private val _draft = MutableLiveData(GameSettingsDraft())
    val draft: LiveData<GameSettingsDraft> = _draft

    private val _titleError = MutableLiveData(false)
    val titleError: LiveData<Boolean> = _titleError

    private val _canSave = MutableLiveData(false)
    val canSave: LiveData<Boolean> = _canSave

    /** Fires once, so the text fields are written by the load and by nobody else after it. */
    private val _loaded = MutableLiveData<Event<GameSettingsDraft>>()
    val loaded: LiveData<Event<GameSettingsDraft>> = _loaded

    private val _dismissEvent = MutableLiveData<Event<Unit>>()
    val dismissEvent: LiveData<Event<Unit>> = _dismissEvent

    private val _deletedEvent = MutableLiveData<Event<Unit>>()
    val deletedEvent: LiveData<Event<Unit>> = _deletedEvent

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    val roundCountOptions: List<SkatRoundCount> =
        SkatRoundCount.ALLOWED_VALUES.map { SkatRoundCount(it) }

    init
    {
        if (gameId == null) _dismissEvent.postValue(Event(Unit)) else load(gameId)
    }

    private fun load(gameId: UUID) = viewModelScope.launch {
        skatGameUseCases.getSkatGame(gameId).fold(
            onSuccess = { game ->
                if (game == null)
                {
                    _dismissEvent.postValue(Event(Unit))
                    return@launch
                }

                val draft = GameSettingsDraft(
                    title = game.title.value,
                    roundCount = game.settings.roundCount,
                    scoringMode = game.settings.scoringMode,
                    roundsPlayed = game.scores.size,
                )

                update(draft)
                _loaded.postValue(Event(draft))
            },
            onFailure = { _dismissEvent.postValue(Event(Unit)) }
        )
    }

    /** A round count can only be chosen if the list has not already passed it. */
    fun isSelectable(roundCount: SkatRoundCount): Boolean =
        roundCount.value >= (_draft.value?.roundsPlayed ?: 0)

    fun setTitle(title: String) = edit { it.copy(title = title) }

    fun setRoundCount(roundCount: SkatRoundCount)
    {
        if (!isSelectable(roundCount)) return
        edit { it.copy(roundCount = roundCount) }
    }

    fun setScoringMode(scoringMode: SkatScoringMode) = edit { it.copy(scoringMode = scoringMode) }

    private fun edit(transform: (GameSettingsDraft) -> GameSettingsDraft)
    {
        update(transform(_draft.value ?: GameSettingsDraft()))
    }

    private fun update(draft: GameSettingsDraft)
    {
        val titleIsValid = Title.create(draft.title.trim()).isSuccess

        _draft.value = draft
        _titleError.value = !titleIsValid
        _canSave.value = titleIsValid && draft.roundCount != null
    }

    fun save()
    {
        val gameId = gameId ?: return
        val draft = _draft.value ?: return
        if (_canSave.value != true) return

        val title = Title.create(draft.title.trim()).getOrNull() ?: return
        val roundCount = draft.roundCount ?: return

        viewModelScope.launch {
            skatGameUseCases.updateSkatGame(
                UpdateSkatGameCommand(
                    id = gameId,
                    title = title,
                    settings = SkatSettings(
                        roundCount = roundCount,
                        scoringMode = draft.scoringMode,
                    ),
                )
            ).fold(
                onSuccess = { _dismissEvent.postValue(Event(Unit)) },
                onFailure = {
                    _errorMessage.postValue(Event(it.message ?: "Could not save settings"))
                }
            )
        }
    }

    /**
     * Deletes the list this sheet was opened on. The fragment confirms first; on success the
     * game screen has nothing left to show, so it closes itself.
     */
    fun deleteGame()
    {
        val gameId = gameId ?: return

        viewModelScope.launch {
            skatGameUseCases.deleteSkatGame(gameId).fold(
                onSuccess = { _deletedEvent.postValue(Event(Unit)) },
                onFailure = {
                    _errorMessage.postValue(Event(it.message ?: "Could not delete list"))
                }
            )
        }
    }

    companion object
    {
        const val ARG_GAME_ID = "gameId"
    }
}
