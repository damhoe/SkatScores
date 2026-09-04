package com.damhoe.skatscores.player.application.repositories

import com.damhoe.skatscores.player.domain.Player
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface PlayersRepository
{
    suspend fun insert(player: Player): Result<Unit>
    suspend fun delete(id: UUID): Result<Player?>
    suspend fun update(player: Player): Result<Unit>
    fun get(id: UUID): Result<Player?>
    suspend fun refreshAll()
    val allPlayers: StateFlow<List<Player>>
}
