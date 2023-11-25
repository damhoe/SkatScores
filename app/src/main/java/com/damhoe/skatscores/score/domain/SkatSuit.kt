package com.damhoe.skatscores.score.domain

enum class SkatSuit {
    GRAND, NULL, CLUBS, SPADES, HEARTS, DIAMONDS, INVALID;

    fun asInteger() = ordinal

    companion object {
        fun fromInteger(value: Int): SkatSuit {
            for (suit in values()) {
                if (suit.ordinal == value) {
                    return suit
                }
            }
            return CLUBS // default
        }
    }
}