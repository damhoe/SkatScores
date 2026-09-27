package com.damhoe.skatscores.game.skat.adapter.presentation

import com.damhoe.skatscores.R
import com.damhoe.skatscores.shared.userError
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.common.SeatError
import com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases
import com.damhoe.skatscores.game.skat.application.usecases.UpdateSkatParticipantsCommand
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
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

@HiltViewModel
class PlayerSeatsViewModel @Inject constructor(
    private val skatGameUseCases: SkatGameUseCases,
    playerUseCases: PlayerUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    private val _seatNames = MutableLiveData(List(SEAT_COUNT) { "" })
    val seatNames: LiveData<List<String>> = _seatNames

    private var loadedNames: List<String>? = null

    /**
     * Unchanged seats are saved as loaded: resolving them by name again would unlink a player
     * renamed since.
     */
    private var loadedSeats: List<Participant> = emptyList()

    private val _seatErrors = MutableLiveData<Map<Int, SeatError>>(emptyMap())
    val seatErrors: LiveData<Map<Int, SeatError>> = _seatErrors

    private val _canSave = MutableLiveData(false)
    val canSave: LiveData<Boolean> = _canSave

    /** One-shot, so only the initial load writes into the fields. */
    private val _loaded = MutableLiveData<Event<List<String>>>()
    val loaded: LiveData<Event<List<String>>> = _loaded

    private val _dismissEvent = MutableLiveData<Event<Unit>>()
    val dismissEvent: LiveData<Event<Unit>> = _dismissEvent

    private val _errorMessage = MutableLiveData<Event<Int>>()
    val errorMessage: LiveData<Event<Int>> = _errorMessage

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

                loadedSeats = game.participants.asList()
                val names = loadedSeats.map { it.displayName }
                loadedNames = names
                update(names)
                _loaded.postValue(Event(names))
            },
            onFailure = { _dismissEvent.postValue(Event(Unit)) }
        )
    }

    fun setSeatName(seat: Int, name: String)
    {
        val names = _seatNames.value ?: List(SEAT_COUNT) { "" }
        update(names.toMutableList().apply { this[seat] = name })
    }

    private fun update(names: List<String>)
    {
        val problems = validateSeats(names)

        _seatNames.value = names
        _seatErrors.value = problems
        _canSave.value = problems.isEmpty()
    }

    /** Names are unique per list in the database, so duplicates are caught here. */
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
        val seats = names.mapIndexed { seat, rawName ->
            val name = PlayerName.create(rawName.trim()).getOrNull() ?: return
            loadedSeats.getOrNull(seat)?.takeIf { it.name == name }?.let { return@mapIndexed it }
            Participant.resolve(name, registered)
        }

        viewModelScope.launch {
            skatGameUseCases.updateSkatParticipants(
                UpdateSkatParticipantsCommand(
                    id = gameId,
                    participants = SkatParticipants(seats[0], seats[1], seats[2]),
                )
            ).fold(
                onSuccess = { _dismissEvent.postValue(Event(Unit)) },
                onFailure = {
                    _errorMessage.postValue(userError(R.string.error_save_players, it))
                }
            )
        }
    }

    companion object
    {
        const val ARG_GAME_ID = "gameId"

        private const val SEAT_COUNT = 3
    }
}
