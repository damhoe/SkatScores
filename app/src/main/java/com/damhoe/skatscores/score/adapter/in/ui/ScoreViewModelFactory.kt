package com.damhoe.skatscores.score.adapter.`in`.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.damhoe.skatscores.score.application.ports.`in`.CreateScoreUseCase
import com.damhoe.skatscores.score.application.ports.`in`.GetScoreUseCase
import com.damhoe.skatscores.score.domain.CreateScoreRequest
import com.damhoe.skatscores.score.domain.ScoreRequest
import com.damhoe.skatscores.score.domain.SkatScoreCommand
import com.damhoe.skatscores.score.domain.UpdateScoreRequest
import java.lang.NullPointerException
import javax.inject.Inject

class ScoreViewModelFactory @Inject constructor(
    val createScoreUseCase: CreateScoreUseCase,
    val getScoreUseCase: GetScoreUseCase
) : ViewModelProvider.Factory {

    private var scoreRequest: ScoreRequest? = null

    fun setCreateRequest(request: CreateScoreRequest) {
        scoreRequest = request
    }

    fun setUpdateRequest(request: UpdateScoreRequest) {
        scoreRequest = request
    }


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ScoreViewModel::class.java) {
            "Only ScoreViewModel allowed in factory method."
        }

        return (scoreRequest?.let {
            var scoreId: Long? = null
            val scoreCommand = when (scoreRequest) {
                is CreateScoreRequest -> {
                    val gameId = (scoreRequest as CreateScoreRequest).gameId
                    SkatScoreCommand().apply { this.gameId = gameId }
                }

                is UpdateScoreRequest -> {
                    scoreId = (scoreRequest as UpdateScoreRequest).scoreId
                    val score = getScoreUseCase.getScore(scoreId).getOrThrow()
                    SkatScoreCommand.fromSkatScore(score)
                }

                else -> throw UnsupportedOperationException(
                    "Unsupported ScoreRequest type: ${it.javaClass}")
            }

            ScoreViewModel(
                createScoreUseCase = createScoreUseCase,
                names = it.names,
                positions = it.positions,
                skatScoreCommand = scoreCommand,
                scoreId = scoreId
            )
        } ?: throw NullPointerException("Score request has not been initialized")) as T
    }


    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return create(modelClass)
    }
}