package com.damhoe.skatscores.game.score.adapter.`in`.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.damhoe.skatscores.game.domain.skat.application.ports.CreateScoreUseCase
import com.damhoe.skatscores.game.domain.score.SkatOutcome
import com.damhoe.skatscores.game.domain.score.SkatScore
import com.damhoe.skatscores.game.domain.score.SkatScoreCommand
import com.damhoe.skatscores.game.domain.score.SkatSuit
import java.lang.IllegalArgumentException

class ScoreViewModel(
    private val createScoreUseCase: CreateScoreUseCase,
    val playerNames: List<String>,
    val playerPositions: List<Int>,
    private val scoreToUpdateId: Long = -1L, // optional
    scoreCommandVal: SkatScoreCommand = SkatScoreCommand()
) : ViewModel() {

    private val scoreCommandLD: MutableLiveData<SkatScoreCommand> =
        MutableLiveData<SkatScoreCommand>().apply { value = scoreCommandVal }

    val scoreCommand: LiveData<SkatScoreCommand> = scoreCommandLD

    var isResultsEnabled: LiveData<Boolean> =
        scoreCommandLD.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isResultsEnabled
        }

    var isSuitsEnabled: LiveData<Boolean> =
        scoreCommandLD.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isSuitsEnabled
        }

    var isSpitzenEnabled: LiveData<Boolean> =
        scoreCommandLD.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isSpitzenEnabled
        }

    var isIncreaseSpitzenEnabled: LiveData<Boolean> =
        scoreCommandLD.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isIncreaseSpitzenEnabled
        }

    var isDecreaseSpitzenEnabled: LiveData<Boolean> =
        scoreCommandLD.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isDecreaseSpitzenEnabled
        }

    var isOuvertEnabled: LiveData<Boolean> =
        scoreCommandLD.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isOuvertEnabled
        }

    var isSchneiderSchwarzEnabled: LiveData<Boolean> =
        scoreCommandLD.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isSchneiderSchwarzEnabled
        }

    var isHandEnabled: LiveData<Boolean> =
        scoreCommandLD.map<SkatScoreCommand, Boolean> { cmd: SkatScoreCommand? ->
            ScoreUIElementsStateManager.fromCommand(cmd).isHandEnabled
        }

    var skatResult: LiveData<SkatOutcome> =
        scoreCommandLD.map<SkatScoreCommand, SkatOutcome> { obj: SkatScoreCommand -> obj.outcome }

    var suit: LiveData<SkatSuit> =
        scoreCommandLD.map<SkatScoreCommand, SkatSuit> { obj: SkatScoreCommand -> obj.suit }

    fun createScore(): SkatScore =
        scoreCommandLD.value?.let { createScoreUseCase.createScore(it).getOrThrow() }
            ?: throw IllegalArgumentException("Score command is null");

    fun updateScore(): SkatScore =
        scoreCommandLD.value?.let{
            createScoreUseCase.updateScore(scoreToUpdateId, it).getOrThrow()
        } ?: throw IllegalArgumentException("Score command is null")

    fun setHand(isHand: Boolean) = scoreCommandLD.value?.let {
        it.isHand = isHand
        scoreCommandLD.postValue(it)
    }

    fun setSchneider(isSchneider: Boolean) = scoreCommandLD.value?.let {
        it.isSchneider = isSchneider
        scoreCommandLD.postValue(it)
    }

    fun setSchneiderAnnounced(isSchneiderAnnounced: Boolean) = scoreCommandLD.value?.let {
        it.isSchneiderAnnounced = isSchneiderAnnounced
        scoreCommandLD.postValue(it)
    }

    fun setSchwarz(isSchwarz: Boolean) = scoreCommandLD.value?.let {
        it.isSchwarz = isSchwarz
        scoreCommandLD.postValue(it)
    }

    fun setSchwarzAnnounced(isSchwarzAnnounced: Boolean) = scoreCommandLD.value?.let {
        it.isSchwarzAnnounced = isSchwarzAnnounced
        scoreCommandLD.postValue(it)
    }


    fun setOuvert(isOuvert: Boolean) = scoreCommandLD.value?.let {
        it.isOuvert = isOuvert
        scoreCommandLD.postValue(it)
    }

    fun setResult(result: SkatOutcome) = scoreCommandLD.value?.let {
        scoreCommandLD.postValue(it.apply {
            if (it.outcome == SkatOutcome.OVERBID) {
                it.suit = SkatSuit.CLUBS
                it.spitzen = 1
            } else if (result == SkatOutcome.OVERBID) {
                it.suit = SkatSuit.NONE
                resetSpitzen()
                resetWinLevels()
            }
            it.outcome = result
        })
    }

    fun setSpitzen(spitzen: Int) = scoreCommandLD.value?.let {
        scoreCommandLD.postValue(it.copy(spitzen = spitzen))
    }

    fun setSuit(suit: SkatSuit) = scoreCommandLD.value?.let {
        scoreCommandLD.postValue(it.copy(suit = suit))
    }

    fun setPlayerPosition(position: Int) = scoreCommandLD.value?.let { currentScore ->
        val updatedScore = currentScore.copy(playerPosition = position).apply {
            if (outcome == SkatOutcome.PASSE) {
                outcome = SkatOutcome.WON
                suit = SkatSuit.CLUBS
                spitzen = 1
            }
        }
        scoreCommandLD.postValue(updatedScore)
    }

    /**
     * Select the player that is mapped to the button at the given position
     */
    fun selectPlayer(position: Int) = scoreCommandLD.value?.let { currentScore ->
        val updatedScore = currentScore.copy(playerPosition = playerPositions[position]).apply {
            if (outcome == SkatOutcome.PASSE) {
                outcome = SkatOutcome.WON
                suit = SkatSuit.CLUBS
                spitzen = 1
            }
        }
        scoreCommandLD.postValue(updatedScore)
    }

    fun setPasse() = scoreCommandLD.value?.let { currentScore ->
        val updatedScore = currentScore.copy(
            outcome = SkatOutcome.PASSE,
            suit = SkatSuit.NONE,
            playerPosition = -1
        ).apply { resetSpitzen(); resetWinLevels() }
        scoreCommandLD.postValue(updatedScore)
    }
}