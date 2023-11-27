package com.damhoe.skatscores.score.adapter.`in`.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.damhoe.skatscores.score.application.ports.`in`.CreateScoreUseCase
import com.damhoe.skatscores.score.domain.SkatOutcome
import com.damhoe.skatscores.score.domain.SkatScore
import com.damhoe.skatscores.score.domain.SkatScoreCommand
import com.damhoe.skatscores.score.domain.SkatSuit
import java.lang.IllegalArgumentException

class ScoreViewModel(
    private val createScoreUseCase: CreateScoreUseCase,
    val playerNames: List<String>,
    val playerPositions: List<Int>,
    private val scoreToUpdateId: Long = -1L, // optional
    scoreCommandVal: SkatScoreCommand = SkatScoreCommand()
) : ViewModel() {
    private val scoreCommand: MutableLiveData<SkatScoreCommand> =
        MutableLiveData<SkatScoreCommand>()

    init {
        this.scoreCommand.value = scoreCommandVal
    }

    var isResultsEnabled: LiveData<Boolean> =
        scoreCommand.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isResultsEnabled
        }

    var isSuitsEnabled: LiveData<Boolean> =
        scoreCommand.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isSuitsEnabled
        }

    var isSpitzenEnabled: LiveData<Boolean> =
        scoreCommand.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isSpitzenEnabled
        }

    var isIncreaseSpitzenEnabled: LiveData<Boolean> =
        scoreCommand.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isIncreaseSpitzenEnabled
        }

    var isDecreaseSpitzenEnabled: LiveData<Boolean> =
        scoreCommand.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isDecreaseSpitzenEnabled
        }

    var isOuvertEnabled: LiveData<Boolean> =
        scoreCommand.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isOuvertEnabled
        }

    var isSchneiderSchwarzEnabled: LiveData<Boolean> =
        scoreCommand.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isSchneiderSchwarzEnabled
        }

    var isHandEnabled: LiveData<Boolean> =
        scoreCommand.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isHandEnabled
        }

    var skatResult: LiveData<SkatOutcome> =
        scoreCommand.map<SkatScoreCommand, SkatOutcome> { obj: SkatScoreCommand -> obj.outcome }

    var suit: LiveData<SkatSuit> =
        scoreCommand.map<SkatScoreCommand, SkatSuit> { obj: SkatScoreCommand -> obj.suit }

    fun getScoreCommand(): LiveData<SkatScoreCommand> {
        return scoreCommand
    }

    fun createScore(): SkatScore =
        scoreCommand.value?.let { createScoreUseCase.createScore(it).getOrThrow() }
            ?: throw IllegalArgumentException("Score command is null");

    fun updateScore(): SkatScore =
        scoreCommand.value?.let{
            createScoreUseCase.updateScore(scoreToUpdateId, it).getOrThrow()
        } ?: throw IllegalArgumentException("Score command is null")

    fun setHand(isHand: Boolean) = scoreCommand.value?.let {
        it.isHand = isHand
        scoreCommand.postValue(it)
    }

    fun setSchneider(isSchneider: Boolean) = scoreCommand.value?.let {
        it.isSchneider = isSchneider
        scoreCommand.postValue(it)
    }

    fun setSchneiderAnnounced(isSchneiderAnnounced: Boolean) = scoreCommand.value?.let {
        it.isSchneiderAnnounced = isSchneiderAnnounced
        scoreCommand.postValue(it)
    }

    fun setSchwarz(isSchwarz: Boolean) = scoreCommand.value?.let {
        it.isSchwarz = isSchwarz
        scoreCommand.postValue(it)
    }

    fun setSchwarzAnnounced(isSchwarzAnnounced: Boolean) = scoreCommand.value?.let {
        it.isSchwarzAnnounced = isSchwarzAnnounced
        scoreCommand.postValue(it)
    }


    fun setOuvert(isOuvert: Boolean) = scoreCommand.value?.let {
        it.isOuvert = isOuvert
        scoreCommand.postValue(it)
    }

    fun setResult(result: SkatOutcome) = scoreCommand.value?.let {
        scoreCommand.postValue(it.apply {
            if (it.outcome == SkatOutcome.OVERBID) {
                it.suit = SkatSuit.CLUBS
                it.spitzen = 1
            } else if (result == SkatOutcome.OVERBID) {
                it.suit = SkatSuit.INVALID
                resetSpitzen()
                resetWinLevels()
            }
            it.outcome = result
        })
    }

    fun setSpitzen(spitzen: Int) = scoreCommand.value?.let {
        scoreCommand.postValue(it.copy(spitzen = spitzen))
    }

    fun setSuit(suit: SkatSuit) = scoreCommand.value?.let {
        scoreCommand.postValue(it.copy(suit = suit))
    }

    fun setPlayerPosition(position: Int) = scoreCommand.value?.let { currentScore ->
        val updatedScore = currentScore.copy(playerPosition = position).apply {
            if (outcome == SkatOutcome.PASSE) {
                outcome = SkatOutcome.WON
                suit = SkatSuit.CLUBS
                spitzen = 1
            }
        }
        scoreCommand.postValue(updatedScore)
    }

    fun setPasse() = scoreCommand.value?.let { currentScore ->
        val updatedScore = currentScore.copy(
            outcome = SkatOutcome.PASSE,
            suit = SkatSuit.INVALID,
            playerPosition = -1
        ).apply { resetSpitzen(); resetWinLevels() }
        scoreCommand.postValue(updatedScore)
    }
}