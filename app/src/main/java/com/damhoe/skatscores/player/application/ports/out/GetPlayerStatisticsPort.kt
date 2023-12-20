package com.damhoe.skatscores.player.application.ports.out

interface GetPlayerStatisticsPort {
    fun getListCount(playerId: Long): Int
    fun getGamesCount(playerId: Long): Int
    fun getSoloCount(playerId: Long): Int
    fun getSoloWinsCount(playerId: Long): Int
    fun getAgainstWinsCount(playerId: Long): Int
}