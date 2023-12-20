package com.damhoe.skatscores.game.score.adapter.`in`.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.damhoe.skatscores.game.score.application.ports.`in`.CreateScoreUseCase
import com.damhoe.skatscores.game.score.application.ports.`in`.GetScoreUseCase
import com.damhoe.skatscores.game.score.domain.ScoreRequest
import com.damhoe.skatscores.game.score.domain.SkatScoreCommand
import javax.inject.Inject

class ScoreViewModelFactory @Inject constructor(
    private val createScoreUseCase: CreateScoreUseCase,
    private val getScoreUseCase: GetScoreUseCase
) : ViewModelProvider.Factory {
    var scoreRequest: ScoreRequest? = null

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ScoreViewModel::class.java) {
            "Only ScoreViewModel allowed in factory method."
        }

        return (scoreRequest?.run {
            when (this) {
                is ScoreRequest.CreateScoreRequest -> {
                    ScoreViewModel(createScoreUseCase, names, positions,
                        scoreCommandVal = SkatScoreCommand().also { it.gameId = gameId }
                    )
                }
                is ScoreRequest.UpdateScoreRequest -> {
                    val score = getScoreUseCase.getScore(scoreId).getOrThrow()
                    ScoreViewModel(createScoreUseCase, names, positions, scoreId,
                        SkatScoreCommand.fromSkatScore(score))
                }
            }
        } ?: throw UnsupportedOperationException("Score request " +
                "must be initialized before creating a view model")) as T
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        create(modelClass)
}