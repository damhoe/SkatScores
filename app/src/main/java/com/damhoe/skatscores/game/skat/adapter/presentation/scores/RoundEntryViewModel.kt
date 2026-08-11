package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.Spitzen
import com.damhoe.skatscores.game.skat.domain.scores.RoundDraft
import com.damhoe.skatscores.game.skat.domain.scores.RoundGame
import com.damhoe.skatscores.game.skat.domain.scores.RoundResult
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Backs the round sheet. Holds one [RoundDraft] and republishes it on every edit, so the
 * points preview and the enabled/visible state of the controls always describe the same round.
 */
@HiltViewModel
class RoundEntryViewModel @Inject constructor(
    private val skatGameUseCases: SkatGameUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    private val editedScoreId: UUID? = savedStateHandle.get<String>(ARG_SCORE_ID)
        ?.let { UUID.fromString(it) }

    private var game: SkatGame? = null

    private val _draft = MutableLiveData<RoundDraft>()
    val draft: LiveData<RoundDraft> = _draft

    private val _participants = MutableLiveData<SkatParticipants>()
    val participants: LiveData<SkatParticipants> = _participants

    /** Round number this sheet is editing, 1-based, for the header. */
    private val _roundNumber = MutableLiveData(1)
    val roundNumber: LiveData<Int> = _roundNumber

    private val _dismissEvent = MutableLiveData<Event<Unit>>()
    val dismissEvent: LiveData<Event<Unit>> = _dismissEvent

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    init
    {
        if (gameId == null) _dismissEvent.postValue(Event(Unit)) else load(gameId)
    }

    private fun load(gameId: UUID) = viewModelScope.launch {
        skatGameUseCases.getSkatGame(gameId).fold(
            onSuccess = { skatGame ->
                if (skatGame == null)
                {
                    _dismissEvent.postValue(Event(Unit))
                    return@launch
                }

                game = skatGame
                _participants.postValue(skatGame.participants)

                val edited = editedScoreId?.let { id -> skatGame.scores.find { it.id == id } }

                if (edited != null)
                {
                    _roundNumber.postValue(skatGame.scores.indexOf(edited) + 1)
                    _draft.postValue(RoundDraft.fromScore(edited, skatGame.participants))
                } else
                {
                    _roundNumber.postValue(skatGame.scores.size + 1)
                    _draft.postValue(RoundDraft.forNewRound(defaultDeclarer(skatGame)))
                }
            },
            onFailure = { _dismissEvent.postValue(Event(Unit)) }
        )
    }

    /** Forehand of the coming round is the most likely declarer, so preselect that seat. */
    private fun defaultDeclarer(skatGame: SkatGame): SkatParticipant
    {
        val seats = skatGame.participants.asList()
        val forehand = Math.floorMod(skatGame.dealerPosition + 1, seats.size)
        return seats[forehand]
    }

    private fun edit(transform: (RoundDraft) -> RoundDraft)
    {
        _draft.value = _draft.value?.let(transform) ?: return
    }

    fun setDeclarer(declarer: SkatParticipant?) = edit { it.copy(declarer = declarer) }

    fun setGame(game: RoundGame) = edit { draft ->
        // Overbid is not representable for a Null game, so fall back to a plain result.
        val result =
            if (game == RoundGame.NULL && draft.result == RoundResult.OVERBID) RoundResult.LOST
            else draft.result

        draft.copy(game = game, result = result)
    }

    fun setSpitzen(value: Int) = edit { draft ->
        runCatching { Spitzen(value) }.fold(
            onSuccess = { draft.copy(spitzen = it) },
            onFailure = { draft }
        )
    }

    fun setHand(enabled: Boolean) = edit { it.copy(hand = enabled) }

    /** Schwarz implies Schneider, so clearing Schneider has to clear Schwarz too. */
    fun setSchneider(enabled: Boolean) = edit { draft ->
        if (enabled) draft.copy(schneider = true)
        else draft.copy(schneider = false, schwarz = false, announced = false)
    }

    fun setSchwarz(enabled: Boolean) = edit { draft ->
        if (enabled) draft.copy(schwarz = true, schneider = true)
        else draft.copy(schwarz = false)
    }

    fun setAnnounced(enabled: Boolean) = edit { it.copy(announced = enabled) }

    fun setOuvert(enabled: Boolean) = edit { it.copy(ouvert = enabled) }

    fun setResult(result: RoundResult) = edit { it.copy(result = result) }

    fun setBid(bid: SkatBid) = edit { it.copy(bid = bid) }

    fun save()
    {
        val draft = _draft.value ?: return
        val game = game ?: return

        viewModelScope.launch {
            val result =
                if (draft.isEditingExistingRound)
                {
                    skatGameUseCases.updateScore(game, draft.toSkatScore())
                } else
                {
                    skatGameUseCases.addScoreToSkatGame(game, draft.toSkatScore(UUID.randomUUID()))
                }

            result.fold(
                onSuccess = { _dismissEvent.postValue(Event(Unit)) },
                onFailure = {
                    _errorMessage.postValue(Event(it.message ?: "Could not save round"))
                }
            )
        }
    }

    companion object
    {
        const val ARG_GAME_ID = "gameId"
        const val ARG_SCORE_ID = "scoreId"
    }
}
