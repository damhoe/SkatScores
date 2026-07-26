package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.Spitzen
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.GrandOrSuit.GrandOrSuitOptions
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SharedScoreViewModel @Inject constructor(
    private val skatGameUseCases: SkatGameUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
{
    private var _skatGame = MutableLiveData<SkatGame>()
    val skatGame: LiveData<SkatGame> = _skatGame

    val skatGameTitle: LiveData<String> = _skatGame.map { it.title.value }
    val skatParticipants: LiveData<SkatParticipants> = _skatGame.map { it.participants }

    private val _dismissEvent = MutableLiveData<Event<Unit>>()
    val dismissEvent: LiveData<Event<Unit>> = _dismissEvent

    val skatGameId = savedStateHandle.get<String>("gameId")
        ?.let { UUID.fromString(it) }
        ?.also { initialize(it) }
        ?: _dismissEvent.postValue(Event(Unit)).let { null }

    val spitzen = MutableLiveData(Spitzen(1))
    val soloPlayer = MutableLiveData<SkatParticipant?>(null)
    val wonOrLost = MutableLiveData(WonOrLost.WON)
    val grandOrSuitOptions = MutableLiveData<GrandOrSuitOptions?>(null)
    val suit = MutableLiveData(SkatSuit.CLUBS)
    val nullOptions = MutableLiveData<SkatScore.Null.NullOptions?>(null)

    fun setSpitzen(value: Spitzen)
    {
        spitzen.value = value
    }

    fun initialize(skatGameId: UUID)
    {
        viewModelScope.launch {
            skatGameUseCases.getSkatGame(skatGameId)
                .onSuccess { skatGame ->
                    if (skatGame == null)
                    {
                        _dismissEvent.postValue(Event(Unit))
                        return@launch
                    }
                    _skatGame.postValue(skatGame)
                }
                .onFailure {
                    _dismissEvent.postValue(Event(Unit))
                }
        }
    }

    fun addPasseScore()
    {
        viewModelScope.launch {
            skatGameUseCases.addScoreToSkatGame(
                skatGame.value!!,
                SkatScore.Passe.create()
            )
        }
    }

    fun dismiss()
    {
        _dismissEvent.postValue(Event(Unit))
    }

    fun addGrandOrSuitScore()
    {
        viewModelScope.launch {
            skatGameUseCases.addScoreToSkatGame(
                skatGame.value!!,
                SkatScore.GrandOrSuit.create(
                    soloPlayer.value!!,
                    wonOrLost.value!!,
                    suit.value!!,
                    spitzen.value!!,
                    grandOrSuitOptions.value,
                )
            )
        }
    }

    fun addNullScore()
    {
        viewModelScope.launch {
            skatGameUseCases.addScoreToSkatGame(
                skatGame.value!!,
                SkatScore.Null.create(
                    soloPlayer = soloPlayer.value!!,
                    wonOrLost = wonOrLost.value!!,
                    options = nullOptions.value,
                )
            )
        }
    }
}