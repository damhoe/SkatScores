package com.damhoe.skatscores.score.adapter.`in`.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.damhoe.skatscores.score.application.ports.`in`.CreateScoreUseCase
import com.damhoe.skatscores.score.domain.ScoreResult
import com.damhoe.skatscores.score.domain.SkatResult
import com.damhoe.skatscores.score.domain.SkatScoreCommand
import com.damhoe.skatscores.score.domain.SkatSuit

class ScoreViewModel(
    private val createScoreUseCase: CreateScoreUseCase,
    val names: List<String>,
    val positions: List<Int>,
    val skatScoreCommand: SkatScoreCommand,
    val scoreId: Long? = null
) : ViewModel() {

    private val scoreCommand: MutableLiveData<SkatScoreCommand> by lazy {
        MutableLiveData<SkatScoreCommand>().apply { value = skatScoreCommand }
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

    var skatResult: LiveData<SkatResult> =
        scoreCommand.map<SkatScoreCommand, SkatResult> { obj: SkatScoreCommand -> obj.result }

    var suit: LiveData<SkatSuit> =
        scoreCommand.map<SkatScoreCommand, SkatSuit> { obj: SkatScoreCommand -> obj.suit }

    fun getScoreCommand(): LiveData<SkatScoreCommand> {
        return scoreCommand
    }

    fun saveScore() : ScoreResult =
        scoreId?.let { updateScore() } ?: createScore()

    private fun createScore(): ScoreResult =
        scoreCommand.value?.let { command ->
            createScoreUseCase.createScore(command).getOrThrow()
                .let { ScoreResult.Create(it) }
        } ?: throw IllegalArgumentException("Score command is null")

    private fun updateScore(): ScoreResult =
        scoreCommand.value?.let{ command ->
            createScoreUseCase.updateScore(scoreId!!, command).getOrThrow()
                .let { ScoreResult.Update(it) }
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

    fun setResult(result: SkatResult) = scoreCommand.value?.let {
        scoreCommand.postValue(it.apply {
            if (it.result == SkatResult.OVERBID) {
                it.suit = SkatSuit.CLUBS
                it.spitzen = 1
            } else if (result == SkatResult.OVERBID) {
                it.suit = SkatSuit.INVALID
                resetSpitzen()
                resetWinLevels()
            }
            it.result = result
        })
    }

    fun setSpitzen(spitzen: Int) = scoreCommand.value?.let {
        scoreCommand.postValue(it.copy(spitzen = spitzen))
    }

    fun setSuit(suit: SkatSuit) = scoreCommand.value?.let {
        scoreCommand.postValue(it.copy(suit = suit))
    }

    fun setPlayerPosition(buttonNumber: Int) = scoreCommand.value?.let { currentScore ->
        val updatedScore = currentScore.copy(playerPosition = positions.get(buttonNumber)).apply {
            if (result == SkatResult.PASSE) {
                result = SkatResult.WON
                suit = SkatSuit.CLUBS
                spitzen = 1
            }
        }
        scoreCommand.postValue(updatedScore)
    }

    fun setPasse() = scoreCommand.value?.let { currentScore ->
        val updatedScore = currentScore.copy(
            result = SkatResult.PASSE,
            suit = SkatSuit.INVALID,
            playerPosition = -1
        ).apply { resetSpitzen(); resetWinLevels() }
        scoreCommand.postValue(updatedScore)
    }
}