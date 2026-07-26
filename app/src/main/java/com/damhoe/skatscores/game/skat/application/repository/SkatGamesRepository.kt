package com.damhoe.skatscores.game.skat.application.repository

import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

interface SkatGamesRepository
{
    suspend fun save(game: SkatGame): Result<Unit>
    suspend fun delete(id: UUID): Result<SkatGame?>
    suspend fun update(game: SkatGame): Result<Unit>
    suspend fun get(id: UUID): Result<SkatGame?>
    fun getAllSince(oldest: Instant): Flow<List<SkatGamePreview>>
    fun getAll(): Flow<List<SkatGamePreview>>
    suspend fun refreshAll()
}