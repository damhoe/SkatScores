package com.damhoe.skatscores.score.adapter.`in`.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.damhoe.skatscores.score.domain.ScoreResult

class ScoreResultViewModel : ViewModel() {
    var scoreResult: MutableLiveData<ScoreResult> = MutableLiveData<ScoreResult>()

    /**
     * Called after creation of the viewModel with activity scope
     * to reset the stored data.
     */
    fun reset() {
        scoreResult = MutableLiveData()
    }
}