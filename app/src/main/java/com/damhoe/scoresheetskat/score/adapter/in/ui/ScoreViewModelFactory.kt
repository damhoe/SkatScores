package com.damhoe.scoresheetskat.score.adapter.`in`.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.damhoe.scoresheetskat.score.application.ports.`in`.CreateScoreUseCase
import com.damhoe.scoresheetskat.score.application.ports.`in`.GetScoreUseCase
import com.damhoe.scoresheetskat.score.domain.ScoreRequest
import com.damhoe.scoresheetskat.score.domain.SkatScore
import javax.inject.Inject

class ScoreViewModelFactory @Inject constructor(
    private val createScoreUseCase: CreateScoreUseCase,
    private val getScoreUseCase: GetScoreUseCase
) : ViewModelProvider.Factory {

    private lateinit var scoreRequest: ScoreRequest

    fun setScoreRequest(scoreRequest: ScoreRequest) {
        this.scoreRequest = scoreRequest
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ScoreViewModel::class.java) {
            "Only ScoreViewModel allowed in factory method."
        }

        return ScoreViewModel.Builder(createScoreUseCase)
            .setRequest(scoreRequest)
            .apply {
                if (scoreRequest.scoreId != -1L) {
                    getScoreUseCase.getScore(scoreRequest.scoreId).onSuccess {
                        fromScore(it)
                    }
                }
            }
            .build() as T
    }


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass == ScoreViewModel::class.java) {
            "Only ScoreViewModel allowed in factory method."
        }

        return ScoreViewModel.Builder(createScoreUseCase)
            .setRequest(scoreRequest)
            .apply {
                if (scoreRequest.scoreId != -1L) {
                    getScoreUseCase.getScore(scoreRequest.scoreId).onSuccess {
                        fromScore(it)
                    }
                }
            }
            .build() as T
    }
}