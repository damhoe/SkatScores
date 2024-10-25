package com.damhoe.skatscores.game.domain.skat.application.ports

import com.damhoe.skatscores.base.Result
import com.damhoe.skatscores.game.domain.score.SkatScore
import com.damhoe.skatscores.game.domain.skat.SkatGame

interface AddScoreToGameUseCase
{
    fun addScoreToGame(
        skatGame: SkatGame?,
        score: SkatScore?)
    
    fun removeLastScore(skatGame: SkatGame?): Result<SkatScore?>?
}
