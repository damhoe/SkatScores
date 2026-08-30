package com.damhoe.skatscores.game.doppelkopf.application.repository

import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGamePreview
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface DoppelkopfGamesRepository
{
    suspend fun save(game: DoppelkopfGame): Result<Unit>
    suspend fun delete(id: UUID): Result<DoppelkopfGame?>
    suspend fun update(game: DoppelkopfGame): Result<Unit>
    suspend fun get(id: UUID): Result<DoppelkopfGame?>
    fun getAll(): Flow<List<DoppelkopfGamePreview>>
}
