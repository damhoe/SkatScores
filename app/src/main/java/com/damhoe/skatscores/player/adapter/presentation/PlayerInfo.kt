package com.damhoe.skatscores.player.adapter.presentation

import com.damhoe.skatscores.player.domain.PlayerName
import java.util.UUID

data class PlayerInfo(
    val playerId: UUID,
    val name: PlayerName,
    val totalGamesPlayed: Int,
)