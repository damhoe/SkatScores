package com.damhoe.skatscores.game.domain.score

import java.util.UUID

abstract class Score(
    var playerPosition: Int = 0,
    var gameId: Long = -1L
) {
    open var id: Long = UUID.randomUUID().mostSignificantBits
    abstract fun toPoints(): Int
}