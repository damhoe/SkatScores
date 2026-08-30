package com.damhoe.skatscores.game.doppelkopf.adapter.presentation.scores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.doppelkopf.application.usecases.DoppelkopfGameUseCases
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfRoundDraft
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfRoundKind
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Backs the Doppelkopf round sheet. Holds one [DoppelkopfRoundDraft] and republishes it on
 * every edit, so the points preview and the enabled state of the controls always describe the
 * same round.
 */
@HiltViewModel
class DoppelkopfRoundEntryViewModel @Inject constructor(
    private val useCases: DoppelkopfGameUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private val gameId: UUID? = savedStateHandle.get<String>(ARG_GAME_ID)
        ?.let { UUID.fromString(it) }

    private val editedScoreId: UUID? = savedStateHandle.get<String>(ARG_SCORE_ID)
        ?.let { UUID.fromString(it) }

    private var game: DoppelkopfGame? = null

    private val _draft = MutableLiveData<DoppelkopfRoundDraft>()
    val draft: LiveData<DoppelkopfRoundDraft> = _draft

    private val _participants = MutableLiveData<DoppelkopfParticipants>()
    val participants: LiveData<DoppelkopfParticipants> = _participants

    /** Whether this list counts the extras collected during play. */
    private val _countsExtraPoints = MutableLiveData(false)
    val countsExtraPoints: LiveData<Boolean> = _countsExtraPoints

    /** Round number this sheet is editing, 1-based, for the header. */
    private val _roundNumber = MutableLiveData(1)
    val roundNumber: LiveData<Int> = _roundNumber

    private val _dismissEvent = MutableLiveData<Event<Unit>>()
    val dismissEvent: LiveData<Event<Unit>> = _dismissEvent

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    val soloKindOptions: List<DoppelkopfSoloKind> = DoppelkopfSoloKind.entries
    val winLevelOptions: List<DoppelkopfWinLevel> = DoppelkopfWinLevel.entries
    val absageOptions: List<DoppelkopfWinLevel> = DoppelkopfWinLevel.Absagen

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

                this@DoppelkopfRoundEntryViewModel.game = game
                _participants.postValue(game.participants)
                _countsExtraPoints.postValue(game.countsExtraPoints)

                val edited = editedScoreId?.let { id -> game.scores.find { it.id == id } }

                if (edited != null)
                {
                    _roundNumber.postValue(game.scores.indexOf(edited) + 1)
                    _draft.postValue(DoppelkopfRoundDraft.fromScore(edited, game.participants))
                } else
                {
                    _roundNumber.postValue(game.scores.size + 1)
                    _draft.postValue(DoppelkopfRoundDraft.forNewRound())
                }
            },
            onFailure = { _dismissEvent.postValue(Event(Unit)) }
        )
    }

    private fun edit(transform: (DoppelkopfRoundDraft) -> DoppelkopfRoundDraft)
    {
        _draft.value = _draft.value?.let(transform) ?: return
    }

    /**
     * Switching between a normal round and a solo clears the other kind's seat choice, so a
     * half-filled normal round cannot leak into a solo.
     */
    fun setKind(kind: DoppelkopfRoundKind) = edit { draft ->
        when (kind)
        {
            DoppelkopfRoundKind.NORMAL -> draft.copy(kind = kind, soloist = null)
            DoppelkopfRoundKind.SOLO -> draft.copy(kind = kind, reSeats = emptySet())
        }
    }

    /**
     * A tap on a seat. In a normal round it moves that seat between Re and Kontra; in a solo
     * it picks who went alone.
     */
    fun toggleSeat(seatIndex: Int)
    {
        val participant = _participants.value?.asList()?.getOrNull(seatIndex) ?: return

        edit { draft ->
            if (draft.isSolo) draft.copy(soloist = participant)
            else draft.toggleReSeat(participant.id)
        }
    }

    fun setSoloKind(kind: DoppelkopfSoloKind) = edit { it.copy(soloKind = kind) }

    fun setWinner(party: DoppelkopfParty) = edit { it.copy(winner = party) }

    fun setWinLevel(level: DoppelkopfWinLevel) = edit { it.copy(winLevel = level) }

    /**
     * What was announced in advance; null is "nothing announced".
     *
     * Deliberately independent of the win level: a party can announce "keine 90" and still
     * lose the round, and the announcement is worth its points to whoever wins either way.
     */
    fun setAbsage(level: DoppelkopfWinLevel?) = edit { it.copy(absage = level) }

    fun setReAnnounced(enabled: Boolean) = edit { it.copy(reAnnounced = enabled) }

    fun setKontraAnnounced(enabled: Boolean) = edit { it.copy(kontraAnnounced = enabled) }

    fun changeExtraPoints(party: DoppelkopfParty, delta: Int) = edit { draft ->
        when (party)
        {
            DoppelkopfParty.RE ->
                draft.copy(extraPointsRe = (draft.extraPointsRe + delta).coerceAtLeast(0))

            DoppelkopfParty.KONTRA ->
                draft.copy(extraPointsKontra = (draft.extraPointsKontra + delta).coerceAtLeast(0))
        }
    }

    fun save()
    {
        val draft = _draft.value ?: return
        val game = game ?: return
        if (!draft.isComplete) return

        viewModelScope.launch {
            val result =
                if (draft.isEditingExistingRound)
                {
                    useCases.updateScore(game, draft.toScore())
                } else
                {
                    useCases.addScore(game, draft.toScore(UUID.randomUUID()))
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
