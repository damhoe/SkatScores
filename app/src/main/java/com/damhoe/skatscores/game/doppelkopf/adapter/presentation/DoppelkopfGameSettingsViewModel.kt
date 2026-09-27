package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import com.damhoe.skatscores.R
import com.damhoe.skatscores.shared.userError
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.doppelkopf.application.usecases.DoppelkopfGameUseCases
import com.damhoe.skatscores.game.doppelkopf.application.usecases.UpdateDoppelkopfGameCommand
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGameDefaults
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfRoundCount
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfScoringMode
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSettings
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DoppelkopfSettingsDraft(
    val title: String = "",
    val roundCount: DoppelkopfRoundCount? = null,
    val scoringMode: DoppelkopfScoringMode = DoppelkopfGameDefaults.ScoringMode,
    /** The round count cannot go below this. */
    val roundsPlayed: Int = 0,
)

@HiltViewModel
class DoppelkopfGameSettingsViewModel @Inject constructor(
    private val useCases: DoppelkopfGameUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    private val _draft = MutableLiveData(DoppelkopfSettingsDraft())
    val draft: LiveData<DoppelkopfSettingsDraft> = _draft

    private val _titleError = MutableLiveData(false)
    val titleError: LiveData<Boolean> = _titleError

    private val _canSave = MutableLiveData(false)
    val canSave: LiveData<Boolean> = _canSave

    /** One-shot, so only the initial load writes into the fields. */
    private val _loaded = MutableLiveData<Event<DoppelkopfSettingsDraft>>()
    val loaded: LiveData<Event<DoppelkopfSettingsDraft>> = _loaded

    private val _dismissEvent = MutableLiveData<Event<Unit>>()
    val dismissEvent: LiveData<Event<Unit>> = _dismissEvent

    private val _deletedEvent = MutableLiveData<Event<Unit>>()
    val deletedEvent: LiveData<Event<Unit>> = _deletedEvent

    private val _errorMessage = MutableLiveData<Event<Int>>()
    val errorMessage: LiveData<Event<Int>> = _errorMessage

    val roundCountOptions: List<DoppelkopfRoundCount> =
        DoppelkopfRoundCount.ALLOWED_VALUES.map { DoppelkopfRoundCount(it) }

    init
    {
        if (gameId == null) _dismissEvent.postValue(Event(Unit)) else load(gameId)
    }

    private fun load(gameId: UUID) = viewModelScope.launch {
        useCases.getGame(gameId).fold(
            onSuccess = { game ->
                if (game == null)
                {
                    _dismissEvent.postValue(Event(Unit))
                    return@launch
                }

                val draft = DoppelkopfSettingsDraft(
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

    fun isSelectable(roundCount: DoppelkopfRoundCount): Boolean =
        roundCount.value >= (_draft.value?.roundsPlayed ?: 0)

    fun setTitle(title: String) = edit { it.copy(title = title) }

    fun setRoundCount(roundCount: DoppelkopfRoundCount)
    {
        if (!isSelectable(roundCount)) return
        edit { it.copy(roundCount = roundCount) }
    }

    fun setScoringMode(scoringMode: DoppelkopfScoringMode) =
        edit { it.copy(scoringMode = scoringMode) }

    private fun edit(transform: (DoppelkopfSettingsDraft) -> DoppelkopfSettingsDraft)
    {
        update(transform(_draft.value ?: DoppelkopfSettingsDraft()))
    }

    private fun update(draft: DoppelkopfSettingsDraft)
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
            useCases.updateGame(
                UpdateDoppelkopfGameCommand(
                    id = gameId,
                    title = title,
                    settings = DoppelkopfSettings(
                        roundCount = roundCount,
                        scoringMode = draft.scoringMode,
                    ),
                )
            ).fold(
                onSuccess = { _dismissEvent.postValue(Event(Unit)) },
                onFailure = {
                    _errorMessage.postValue(userError(R.string.error_save_settings, it))
                }
            )
        }
    }

    fun deleteGame()
    {
        val gameId = gameId ?: return

        viewModelScope.launch {
            useCases.deleteGame(gameId).fold(
                onSuccess = { _deletedEvent.postValue(Event(Unit)) },
                onFailure = {
                    _errorMessage.postValue(userError(R.string.error_delete_list, it))
                }
            )
        }
    }

    companion object
    {
        const val ARG_GAME_ID = "gameId"
    }
}
