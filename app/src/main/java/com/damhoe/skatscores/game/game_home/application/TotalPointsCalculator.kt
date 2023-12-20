package com.damhoe.skatscores.game.game_home.application

import com.damhoe.skatscores.game.score.Constant
import com.damhoe.skatscores.game.score.domain.SkatScore

object TotalPointsCalculator {
    private const val BONUS_AGAINST = 40
    private const val BONUS_SOLO = 50

    fun createPointsHistory(
        playerCount: Int,
        scores: List<SkatScore>,
        isTournamentScoring: Boolean
    ) : List<List<Int>> {

        val pointsHistory: MutableList<MutableList<Int>> = mutableListOf()

        for (k in 0..<playerCount) {
            pointsHistory.add(mutableListOf(0))
        }

        for (score in scores) {
            for (k in 0..<playerCount) {
                // Get points for each player
                var playerPoints = pointsHistory[k].lastOrNull() ?: 0
                playerPoints += pointsForPlayer(k, score, isTournamentScoring)
                // Add points to history
                pointsHistory[k].add(playerPoints)
            }
        }

        return pointsHistory
    }

    private fun pointsForPlayer(
        position: Int,
        score: SkatScore,
        isTournamentScoring: Boolean
    ): Int {
        val isSoloPlayer = score.playerPosition == position
        var points = if (isSoloPlayer) score.toPoints() else 0

        if (isTournamentScoring) {
            points += bonusForPlayer(position, score)
        }

        return points
    }

    private fun bonusForPlayer(position: Int, score: SkatScore) =
        againstBonusForPlayer(position, score.playerPosition) + soloBonusForPlayer(position, score)

    private fun againstBonusForPlayer(position: Int, soloPosition: Int) =
        if (position == soloPosition) 0 else BONUS_AGAINST

    private fun soloBonusForPlayer(position: Int, score: SkatScore): Int {
        if (position != score.playerPosition) {
            return 0
        }

        return when {
            score.isPasse -> 0
            score.isWon -> BONUS_SOLO
            else -> -BONUS_SOLO
        }
    }

    fun calculateTotalPoints(
        numberOfPlayers: Int,
        scores: List<SkatScore>,
        isTournamentScoring: Boolean
    ): IntArray {
        val points = IntArray(numberOfPlayers)
        for (k in scores.indices) {
            val score = scores[k]
            if (score.isPasse) continue
            val scorePoints = score.toPoints()
            points[score.playerPosition] += scorePoints
        }
        if (isTournamentScoring) {
            val pointsLossOfOthers = calculateLossOfOthersBonus(numberOfPlayers, scores)
            val pointsWin = calculateWinBonus(numberOfPlayers, scores)
            for (i in 0 until numberOfPlayers) {
                points[i] += pointsLossOfOthers[i] + pointsWin[i]
            }
        }
        return points
    }

    fun calculateLossOfOthersBonus(numberOfPlayers: Int, scores: List<SkatScore>): IntArray {
        val points = IntArray(numberOfPlayers)
        for (k in scores.indices) {
            val score = scores[k]
            if (score.isPasse) {
                continue
            }
            if (score.isWon) {
                continue
            }
            for (j in 0 until numberOfPlayers) {
                if (j != score.playerPosition) {
                    points[j] += Constant.BONUS_LOSS_OTHERS
                }
            }
        }
        return points
    }

    fun calculateWinBonus(numberOfPlayers: Int, scores: List<SkatScore>): IntArray {
        val points = IntArray(numberOfPlayers)
        for (k in scores.indices) {
            val score = scores[k]
            if (score.isPasse) {
                continue
            }
            var bonus = Constant.BONUS_WIN
            if (!score.isWon) {
                bonus *= -1
            }
            points[score.playerPosition] += bonus
        }
        return points
    }
}
