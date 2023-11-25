package com.damhoe.skatscores.score.domain

enum class SkatResult {
    WON, LOST, OVERBID, PASSE;

    fun asInteger() = ordinal

    companion object {
        fun fromInteger(value: Int): SkatResult {
            for (result in values()) {
                if (result.ordinal == value) {
                    return result
                }
            }
            return PASSE // default
        }
    }
}