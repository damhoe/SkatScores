package com.damhoe.skatscores.game.domain

import arrow.core.Either
import com.damhoe.skatscores.player.domain.Player

open class PlayerList private constructor(
    val count: UInt,
    var players: MutableList<Player>)
{
    companion object
    {
        operator fun invoke(
            count: UInt): Either<InvalidCount, PlayerList>
        {
            return when
            {
                count > 0u -> Either.Right(
                    PlayerList(
                        count,
                        mutableListOf()))
                else -> Either.Left(
                    InvalidCount(
                        count));
            }
        }
    }

    fun Set(
        index: Int,
        player: Player)
    {
        players[index] = player
    }

    data class InvalidCount(
        val count: UInt);
}