package com.damhoe.skatscores.score.domain

enum class SkatSuit {
    GRAND, NULL, CLUBS, SPADES, HEARTS, DIAMONDS, INVALID;

    fun asInteger(): Int = ordinal

    companion object {
        fun fromInteger(value: Int): SkatSuit = values().find { it.ordinal == value } ?: CLUBS
    }
}