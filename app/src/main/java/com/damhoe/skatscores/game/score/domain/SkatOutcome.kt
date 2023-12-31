package com.damhoe.skatscores.game.score.domain

enum class SkatOutcome {
    WON, LOST, OVERBID, PASSE;

    fun asInteger(): Int = ordinal

    companion object {
        fun fromInteger(value: Int): SkatOutcome =
            values().find { it.ordinal == value } ?: PASSE
    }
}