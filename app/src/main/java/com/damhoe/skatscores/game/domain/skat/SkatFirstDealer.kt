package com.damhoe.skatscores.game.domain.skat

import android.os.Parcelable
import arrow.core.Either
import kotlinx.parcelize.Parcelize

@Parcelize
class SkatFirstDealer private constructor(
    val value : Int) : Parcelable
{
    companion object
    {
        fun create(
            count: Int,
            playerCount: SkatPlayerCount) : Either<NotInPlayerCountRange, SkatFirstDealer>
        {
            if (isInPlayerCountRange(
                    count,
                    playerCount))
            {
                return Either.Right(
                    SkatFirstDealer(
                        count))
            }

            return Either.Left(
                NotInPlayerCountRange(
                    count,
                    playerCount))
        }

        private fun isInPlayerCountRange(
            count: Int,
            playerCount: SkatPlayerCount) = count < playerCount.value && count >= 0
    }

}

data class NotInPlayerCountRange(
    val firstDealer: Int,
    val playerCount: SkatPlayerCount)