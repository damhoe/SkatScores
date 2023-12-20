package com.damhoe.skatscores.player.application

import com.damhoe.skatscores.player.application.ports.`in`.GetPlayerStatisticsUseCase
import com.damhoe.skatscores.player.application.ports.out.GetPlayerStatisticsPort
import com.damhoe.skatscores.player.domain.PlayerStatistics
import javax.inject.Inject

class PlayerStatisticsService @Inject constructor(
    val adapter: GetPlayerStatisticsPort
) : GetPlayerStatisticsUseCase {

    override fun getPlayerStatistics(playerId: Long): PlayerStatistics =
        adapter.run {
            val listCount = getListCount(playerId)
            val gamesCount = getGamesCount(playerId)
            val soloCount = getSoloCount(playerId)
            val soloWinsCount = getSoloWinsCount(playerId)
            val againstWinsCount = getAgainstWinsCount(playerId)

            PlayerStatistics(
                listCount = listCount,
                gamesCount = gamesCount,
                soloPercentage = nullSavePercentageDivision(soloCount, gamesCount),
                soloWinPercentage = nullSavePercentageDivision(soloWinsCount, soloCount),
                againstWinPercentage = nullSavePercentageDivision(againstWinsCount, (gamesCount - soloCount))
            )
        }

    private fun nullSavePercentageDivision(a: Int, b: Int) =
        if (b == 0) {
            0.0
        } else {
            a * 100.0 / b
        }
}