package com.damhoe.skatscores.game.domain.score

enum class SkatSuit {
    GRAND, NULL, CLUBS, SPADES, HEARTS, DIAMONDS, INVALID;

    fun asInteger(): Int = ordinal

    companion object {
        fun fromInteger(value: Int): SkatSuit = values().find { it.ordinal == value } ?: CLUBS
    }
}