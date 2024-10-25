package com.damhoe.skatscores

import com.damhoe.skatscores.game.domain.skat.application.ports.CreateScoreUseCase
import com.damhoe.skatscores.game.domain.skat.application.ports.GetScoreUseCase
import com.damhoe.skatscores.game.domain.score.SkatScore

class KotlinResultWrapper {

    companion object {
        fun getScores(getScoreUseCase: GetScoreUseCase, id: Long): List<SkatScore> =
            getScoreUseCase.getSkatGameScores(id).getOrThrow()

        fun deleteScoresForGame(createScoreUseCase: CreateScoreUseCase, id: Long): Int =
            createScoreUseCase.deleteScoresForGame(id).getOrThrow()

        fun deleteScore(createScoreUseCase: CreateScoreUseCase, id: Long): SkatScore =
            createScoreUseCase.deleteScore(id).getOrThrow()
    }
}