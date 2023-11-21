package com.damhoe.skatscores.score.domain

import com.damhoe.skatscores.score.Constant

data class SkatScoreCommand(
    var spitzen: Int = 1,
    var suit: SkatSuit = SkatSuit.INVALID, // Clubs, Diamonds, Hears, Spades, Null, Grand
    var result: SkatResult = SkatResult.PASSE, // Won, Lost, Overbid, Passe
    var isHand: Boolean = false,
    var isSchneider: Boolean = false,
    var isSchneiderAnnounced: Boolean = false,
    var isSchwarz: Boolean = false,
    var isSchwarzAnnounced: Boolean = false,
    var isOuvert: Boolean = false,
    var playerPosition: Int = Constant.PASSE_PLAYER_POSITION,
    var gameId: Long = -1L
) {

    val isMinSpitzen: Boolean
        get() = spitzen <= getMinSpitzen()
    val isMaxSpitzen: Boolean
        get() = spitzen >= getMaxSpitzen()

    private fun getMinSpitzen() = 1

    private fun getMaxSpitzen(): Int {
        return if (suit == SkatSuit.GRAND) {
            4
        } else 11
    }

    val isPasse: Boolean
        get() = playerPosition == Constant.PASSE_PLAYER_POSITION
    val isWon: Boolean
        get() = SkatResult.WON == result
    val isOverbid: Boolean
        get() = SkatResult.OVERBID == result
    val isLost: Boolean
        get() = SkatResult.LOST == result

    fun resetWinLevels() {
        isHand = false
        isOuvert = false
        isSchwarz = false
        isSchwarzAnnounced = false
        isSchneider = false
        isSchneiderAnnounced = false
    }

    fun resetSpitzen() {
        spitzen = 1
    }

    companion object {
        fun fromSkatScore(score: SkatScore) = SkatScoreCommand().apply {
            isHand = score.isHand
            isOuvert = score.isOuvert
            result = score.result
            isSchneider = score.isSchneider
            isSchwarz = score.isSchwarz
            suit = score.suit
            spitzen = score.spitzen
            isSchneiderAnnounced = score.isSchneiderAnnounced
            isSchwarzAnnounced = score.isSchwarzAnnounced
            playerPosition = score.getPlayerPosition()
            gameId = score.getGameId()
        }
    }
}