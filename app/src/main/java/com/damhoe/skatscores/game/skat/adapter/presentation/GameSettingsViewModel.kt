package com.damhoe.skatscores.game.skat.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases
import com.damhoe.skatscores.game.skat.application.usecases.UpdateSkatGameCommand
import com.damhoe.skatscores.game.skat.application.usecases.UpdateSkatParticipantsCommand
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.player.application.usecases.PlayerUseCases
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** Which seat a validation problem belongs to. */
enum class SeatError
{
    NAME_REQUIRED,
    NAME_DUPLICATE,
}

data class GameSettingsDraft(
    val title: String = "",
    val roundCount: SkatRoundCount? = null,
    val scoringMode: SkatScoringMode = SkatScoringMode.CLASSIC,
    /** The three seats, in seat order. */
    val seatNames: List<String> = listOf("", "", ""),
    /** Rounds already played; the list cannot be shortened below this. */
    val roundsPlayed: Int = 0,
)

/**
 * Everything about a running list that is not a round: who sits at the three seats, the name,
 * the round count and the scoring mode. Seats used to have a sheet of their own, which put two
 * doors on the same room.
 */
@HiltViewModel
class GameSettingsViewModel @Inject constructor(
    private val skatGameUseCases: SkatGameUseCases,
    playerUseCases: PlayerUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    private val _draft = MutableLiveData(GameSettingsDraft())
    val draft: LiveData<GameSettingsDraft> = _draft

    /** What load() found, so save() can skip the writes nothing changed. */
    private var loadedDraft: GameSettingsDraft? = null

    private val _titleError = MutableLiveData(false)
    val titleError: LiveData<Boolean> = _titleError

    /** Seat index to problem, empty while the seats are ready to save. */
    private val _seatErrors = MutableLiveData<Map<Int, SeatError>>(emptyMap())
    val seatErrors: LiveData<Map<Int, SeatError>> = _seatErrors

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

    val allRegisteredPlayers: StateFlow<List<Player>> = playerUseCases.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
                    seatNames = game.participants.asList().map { it.displayName },
                    roundsPlayed = game.scores.size,
                )

                loadedDraft = draft
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

    fun setSeatName(seat: Int, name: String) = edit {
        it.copy(seatNames = it.seatNames.toMutableList().apply { this[seat] = name })
    }

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
        val seatProblems = validateSeats(draft.seatNames)

        _draft.value = draft
        _titleError.value = !titleIsValid
        _seatErrors.value = seatProblems
        _canSave.value = titleIsValid && draft.roundCount != null && seatProblems.isEmpty()
    }

    /**
     * Names have to be present and distinct: participants are unique per (list, name) in the
     * database, so duplicates would fail the update rather than the user.
     */
    private fun validateSeats(names: List<String>): Map<Int, SeatError>
    {
        val trimmed = names.map { it.trim() }
        val problems = mutableMapOf<Int, SeatError>()

        trimmed.forEachIndexed { seat, name ->
            when
            {
                PlayerName.create(name).isFailure -> problems[seat] = SeatError.NAME_REQUIRED

                trimmed.indexOfFirst { it.equals(name, ignoreCase = true) } != seat ->
                    problems[seat] = SeatError.NAME_DUPLICATE
            }
        }

        return problems
    }

    fun save()
    {
        val gameId = gameId ?: return
        val draft = _draft.value ?: return
        if (_canSave.value != true) return

        val title = Title.create(draft.title.trim()).getOrNull() ?: return
        val roundCount = draft.roundCount ?: return
        val registered = allRegisteredPlayers.value
        val seats = draft.seatNames.map { rawName ->
            val name = PlayerName.create(rawName.trim()).getOrNull() ?: return
            // A name matching a registered player keeps that seat linked to the profile;
            // the dialog this replaces always fell back to a guest and lost the link.
            SkatParticipant.resolve(name, registered)
        }

        viewModelScope.launch {
            // Seats go first: UpdateSkatGameUseCase reads the game back before writing, so it
            // carries the new participants rather than the ones the sheet opened with.
            if (draft.seatNames != loadedDraft?.seatNames)
            {
                skatGameUseCases.updateSkatParticipants(
                    UpdateSkatParticipantsCommand(
                        id = gameId,
                        participants = SkatParticipants(seats[0], seats[1], seats[2]),
                    )
                ).onFailure {
                    _errorMessage.postValue(Event(it.message ?: "Could not save players"))
                    return@launch
                }
            }

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
