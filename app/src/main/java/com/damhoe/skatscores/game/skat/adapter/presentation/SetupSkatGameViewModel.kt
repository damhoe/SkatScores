package com.damhoe.skatscores.game.skat.adapter.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.usecases.CreateSkatGameCommand
import com.damhoe.skatscores.game.skat.application.usecases.CreateSkatGameUseCase
import com.damhoe.skatscores.game.skat.application.usecases.SkatGameUseCases
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatGameDefaults
import com.damhoe.skatscores.game.skat.domain.SkatPlayerCount
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SetupSkatGameViewModel @Inject constructor(
    private val createSkatGame: CreateSkatGameUseCase
) : ViewModel()
{
    val title = MutableLiveData("")
    val playerCount = MutableLiveData(SkatGameDefaults.PlayerCount)
    val useTournamentScoring = MutableLiveData(false)
    val roundCount = MutableLiveData(SkatGameDefaults.RoundCount)

    private val _createSkatGameCommand = MediatorLiveData<CreateSkatGameCommand?>()
    val createSkatGameCommand: LiveData<CreateSkatGameCommand?> = _createSkatGameCommand

    var canCreateSkatGame: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(_createSkatGameCommand) { command -> value = command != null }
    }

    private val _navigateToGame = MutableLiveData<Event<SkatGame>>()
    val navigateToGame: LiveData<Event<SkatGame>> = _navigateToGame

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    init
    {
        _createSkatGameCommand.addSource<String?>(title) { validateAndCreateCommand() }
        _createSkatGameCommand.addSource<SkatPlayerCount>(playerCount) { validateAndCreateCommand() }
        _createSkatGameCommand.addSource<Boolean>(useTournamentScoring) { validateAndCreateCommand() }
        _createSkatGameCommand.addSource<SkatRoundCount>(roundCount) { validateAndCreateCommand() }

        validateAndCreateCommand()
    }

    /** @noinspection DataFlowIssue
     */
    private fun validateAndCreateCommand()
    {
        val title = Title.create(title.value!!)

        val command = title.map { title ->
            CreateSkatGameCommand(
                settings = SkatSettings(
                    roundCount = roundCount.value!!,
                    scoringMode =
                        if (useTournamentScoring.value!!)
                            SkatScoringMode.TOURNAMENT
                        else SkatScoringMode.CLASSIC
                ),
                title = title,
                playerCount = playerCount.value!!
            )
        }.getOrNull()

        _createSkatGameCommand.postValue(command)
    }

    fun handle(command: CreateSkatGameCommand)
    {
        viewModelScope.launch {
            checkNotNull(command)
            createSkatGame(command).fold(
                onSuccess = { game ->
                    _navigateToGame.postValue(Event(game))
                },
                onFailure = { exception ->
                    _errorMessage.postValue(
                        Event("Failed to create skat game: ${exception.message}"))
                }
            )
        }
    }
}