package com.damhoe.skatscores.game.score.adapter.out

import com.damhoe.skatscores.game.score.domain.SkatOutcome
import com.damhoe.skatscores.game.score.domain.SkatScore
import com.damhoe.skatscores.game.score.domain.SkatSuit


data class ScoreDto(
    // General properties
    var id: Long = 0,
    var gameId: Long = 0,
    var playerPosition: Int = 0,

    // Skat specific properties
    var spitzen: Int = 0, // negative value if missing
    var suit: Int = 0, // Clubs, Diamonds, Hears, Spades, Null, Grand
    var hand: Int = 0,
    var schneider: Int = 0,
    var schneiderAnnounced: Int = 0,
    var schwarz: Int = 0,
    var schwarzAnnounced: Int = 0,
    var ouvert: Int = 0,
    var result: Int = 0
) {
    fun toScore() = SkatScore().apply {
        id = this@ScoreDto.id
        gameId = this@ScoreDto.gameId
        playerPosition = this@ScoreDto.playerPosition
        spitzen = this@ScoreDto.spitzen
        suit = SkatSuit.fromInteger(this@ScoreDto.suit)
        isHand = hand == 1
        isSchneider = schneider == 1
        isSchneiderAnnounced = schneiderAnnounced == 1
        isSchwarz = schwarz == 1
        isSchwarzAnnounced = schwarzAnnounced == 1
        isOuvert = ouvert == 1
        outcome = SkatOutcome.fromInteger(this@ScoreDto.result)
    }

    companion object {
        fun fromScore(skatScore: SkatScore) = ScoreDto().apply {
            id = skatScore.id
            gameId = skatScore.gameId
            playerPosition = skatScore.playerPosition
            spitzen = skatScore.spitzen
            suit = skatScore.suit.asInteger()
            hand = if (skatScore.isHand) 1 else 0
            schneider = if (skatScore.isSchneider) 1 else 0
            schneiderAnnounced = if (skatScore.isSchneiderAnnounced) 1 else 0
            schwarz = if (skatScore.isSchwarz) 1 else 0
            schwarzAnnounced = if (skatScore.isSchwarzAnnounced) 1 else 0
            ouvert = if (skatScore.isOuvert) 1 else 0
            result = skatScore.outcome.asInteger()
        }
    }
}