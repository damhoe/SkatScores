package com.damhoe.skatscores.game.skat.domain

import com.damhoe.skatscores.game.skat.domain.scores.SkatScore

data class AddedScoreToGame(
    val score: SkatScore,
    val round: Int,
    val updatedSkatGame: SkatGame,
)