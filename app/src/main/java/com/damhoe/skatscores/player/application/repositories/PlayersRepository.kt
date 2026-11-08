package com.damhoe.skatscores.player.application.repositories

import com.damhoe.skatscores.player.domain.Player
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface PlayersRepository
{
    suspend fun insert(player: Player): Result<Unit>

    /** The player's seats stay as guests. */
    suspend fun delete(id: UUID): Result<Player?>

    /** Also renames the seats linked to the player. */
    suspend fun update(player: Player): Result<Unit>

    suspend fun get(id: UUID): Result<Player?>

    val allPlayers: StateFlow<List<Player>>
}
