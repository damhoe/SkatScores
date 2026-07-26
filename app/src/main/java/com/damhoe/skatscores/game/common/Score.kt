package com.damhoe.skatscores.game.common

import java.util.UUID

abstract class Score(
    open val id: UUID)
{
    abstract fun toPoints(): Int
}