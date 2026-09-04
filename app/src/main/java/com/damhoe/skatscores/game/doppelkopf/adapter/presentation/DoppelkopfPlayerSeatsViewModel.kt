package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.common.SeatError
import com.damhoe.skatscores.game.doppelkopf.application.usecases.DoppelkopfGameUseCases
import com.damhoe.skatscores.game.doppelkopf.application.usecases.UpdateDoppelkopfParticipantsCommand
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
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

/**
 * Who sits at the four seats of a running Doppelkopf list. Its own screen rather than a section
 * of the settings sheet, for the same reason as
 * [com.damhoe.skatscores.game.skat.adapter.presentation.PlayerSeatsViewModel]: swapping a
 * player is the edit that happens mid-evening.
 */
@HiltViewModel
class DoppelkopfPlayerSeatsViewModel @Inject constructor(
    private val useCases: DoppelkopfGameUseCases,
    playerUseCases: PlayerUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    private val _seatNames =
        MutableLiveData(List(DoppelkopfParticipants.SEAT_COUNT) { "" })
    val seatNames: LiveData<List<String>> = _seatNames

    /** What load() found, so save() can skip a write nothing changed. */
    private var loadedNames: List<String>? = null

    /** Seat index to problem, empty while the seats are ready to save. */
    private val _seatErrors = MutableLiveData<Map<Int, SeatError>>(emptyMap())
    val seatErrors: LiveData<Map<Int, SeatError>> = _seatErrors

    private val _canSave = MutableLiveData(false)
    val canSave: LiveData<Boolean> = _canSave

    /** Fires once, so the text fields are written by the load and by nobody else after it. */
    private val _loaded = MutableLiveData<Event<List<String>>>()
    val loaded: LiveData<Event<List<String>>> = _loaded

    private val _dismissEvent = MutableLiveData<Event<Unit>>()
    val dismissEvent: LiveData<Event<Unit>> = _dismissEvent

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    val allRegisteredPlayers: StateFlow<List<Player>> = playerUseCases.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

                val names = game.participants.asList().map { it.displayName }
                loadedNames = names
                update(names)
                _loaded.postValue(Event(names))
            },
            onFailure = { _dismissEvent.postValue(Event(Unit)) }
        )
    }

    fun setSeatName(seat: Int, name: String)
    {
        val names = _seatNames.value ?: List(DoppelkopfParticipants.SEAT_COUNT) { "" }
        update(names.toMutableList().apply { this[seat] = name })
    }

    private fun update(names: List<String>)
    {
        val problems = validateSeats(names)

        _seatNames.value = names
        _seatErrors.value = problems
        _canSave.value = problems.isEmpty()
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
        val names = _seatNames.value ?: return
        if (_canSave.value != true) return

        if (names == loadedNames)
        {
            _dismissEvent.value = Event(Unit)
            return
        }

        val registered = allRegisteredPlayers.value
        val seats = names.map { rawName ->
            val name = PlayerName.create(rawName.trim()).getOrNull() ?: return
            // A name matching a registered player keeps that seat linked to the profile, so a
            // player's record follows them from list to list and across both games.
            Participant.resolve(name, registered)
        }

        viewModelScope.launch {
            useCases.updateParticipants(
                UpdateDoppelkopfParticipantsCommand(
                    id = gameId,
                    participants = DoppelkopfParticipants(seats),
                )
            ).fold(
                onSuccess = { _dismissEvent.postValue(Event(Unit)) },
                onFailure = {
                    _errorMessage.postValue(Event(it.message ?: "Could not save players"))
                }
            )
        }
    }

    companion object
    {
        const val ARG_GAME_ID = "gameId"
    }
}
