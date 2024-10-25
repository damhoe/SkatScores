package com.damhoe.skatscores.game.domain.skat

import android.os.Parcelable
import arrow.core.Either
import com.damhoe.skatscores.game.domain.skat.SkatRoundCount.Constants.ValidRoundCounts
import kotlinx.parcelize.Parcelize

@Parcelize
class SkatRoundCount private constructor(
    val value : Int) : Parcelable
{
    companion object
    {
        fun create(
            count: Int) : Either<InvalidSkatRoundCount, SkatRoundCount>
        {
            if (isValidSkatRoundCount(count)) {
                return Either.Right(
                    SkatRoundCount(
                        count))
            }

            return Either.Left(
                InvalidSkatRoundCount(
                    count))
        }

        private fun isValidSkatRoundCount(
            count: Int) = count in ValidRoundCounts
    }

    private object Constants
    {
        val ValidRoundCounts = arrayOf(
            3,
            9,
            12,
            18,
            24,
            32);
    }
}

data class InvalidSkatRoundCount(
    val count: Int)