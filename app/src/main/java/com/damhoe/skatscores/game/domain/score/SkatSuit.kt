package com.damhoe.skatscores.game.domain.score

import arrow.core.Either
import arrow.core.toOption

enum class SkatSuit
{
    GRAND,
    NULL,
    CLUBS,
    SPADES,
    HEARTS,
    DIAMONDS,
    NONE;

    fun toInt(): Int = ordinal

    companion object
    {
        fun fromInt(
            value: Int): Either<IntDoesNotMatchAnySkatSuit, SkatSuit> =
            entries
                .find { it.ordinal == value }
                .toOption()
                .toEither { IntDoesNotMatchAnySkatSuit(value) }
    }
}

data class IntDoesNotMatchAnySkatSuit(
    val value: Int)