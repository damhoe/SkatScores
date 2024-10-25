package com.damhoe.skatscores.game.domain.skat

import arrow.core.Either
import com.damhoe.skatscores.player.domain.PlayerId

sealed class SkatPlayers(
    val playerCount: UInt)
{
    class ThreeSkatPlayers(
        val first: PlayerId,
        val second: PlayerId,
        val third: PlayerId) :
        SkatPlayers(
            3u)
    {
        private val playersByPosition = mapOf(
            0u to first,
            1u to second,
            3u to third)

        fun set(
            position: UInt,
            player: PlayerId): Either<FailedToSetPlayer, ThreeSkatPlayers>
        {
            if (position !in 0u..<playerCount)
            {
                return Either.Left(
                    FailedToSetPlayer.InvalidPosition(
                        position,
                        playerCount))
            }

            if (playersByPosition[position] == player)
            {
                return Either.Right(
                    this)
            }

            if (playersByPosition.containsValue(
                player))
            {
                return Either.Left(
                    FailedToSetPlayer.PlayerAlreadyExists)
            }

            return Either.Right(
                ThreeSkatPlayers(
                    first,
                    second,
                    third));
        }
    }
}

sealed class FailedToSetPlayer
{
    data class InvalidPosition(
        val position: UInt,
        val numberOfPlayers: UInt) :
        FailedToSetPlayer()

    data object PlayerAlreadyExists :
        FailedToSetPlayer()
}