package com.damhoe.skatscores.score.application

import com.damhoe.skatscores.score.Constant
import com.damhoe.skatscores.score.domain.SkatScore
import com.damhoe.skatscores.score.domain.SkatSuit

class PointsCalculator(private val score: SkatScore) {

    fun calculatePoints(): Int = when {
        score.isPasse -> 0
        score.isOverbid -> -50
        else -> calculatePlayedGamePoints()
    }

    private fun calculatePlayedGamePoints(): Int = when (score.suit) {
        SkatSuit.NULL -> calculateNullPoints()
        else -> calculateTrumpGamePoints()
    }

    private fun calculateNullPoints(): Int = when {
        score.isHand && score.isOuvert -> Constant.POINTS_NULL_OUVERT_HAND
        score.isHand -> Constant.POINTS_NULL_HAND
        score.isOuvert -> Constant.POINTS_NULL_OUVERT
        else -> Constant.POINTS_NULL
    }.let { if (score.isWon) it else -2 * it }

    private fun calculateTrumpGamePoints(): Int = when (score.suit) {
        SkatSuit.CLUBS -> Constant.POINTS_CLUBS
        SkatSuit.HEARTS -> Constant.POINTS_HEARTS
        SkatSuit.DIAMONDS -> Constant.POINTS_DIAMONDS
        SkatSuit.SPADES -> Constant.POINTS_SPADES
        SkatSuit.GRAND -> Constant.POINTS_GRAND
        else -> throw IllegalArgumentException("Invalid suit: ${score.suit}")
    }.let { spielwert * it }

    private var spielwert: Int = listOf(
        score.isHand,
        score.isOuvert,
        score.isSchneider,
        score.isSchneiderAnnounced,
        score.isSchwarz,
        score.isSchwarzAnnounced
    ).count{ it }.let { (score.spitzen + 1 + it) }
        .let { if (score.isWon) it else -2 * it }
}