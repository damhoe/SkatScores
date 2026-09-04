package com.damhoe.skatscores.game.common

data class GameRunStateInfo(
    val roundsCount: Int,
    var currentRound: Int,
    var isFinished: Boolean
)