package com.damhoe.skatscores.game.skat.application.repository

import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

interface SkatGamesRepository
{
    suspend fun save(game: SkatGame): Result<Unit>

    /** Deleting a missing list succeeds. */
    suspend fun delete(id: UUID): Result<Unit>

    suspend fun updateSettings(id: UUID, title: Title, settings: SkatSettings): Result<Unit>

    /** Seats keep their participant ids, so recorded rounds follow the new names. */
    suspend fun updateSeats(id: UUID, participants: SkatParticipants): Result<Unit>

    suspend fun get(id: UUID): Result<SkatGame?>

    /** Emits after every write to the list; null once it is gone. */
    fun observe(id: UUID): Flow<SkatGame?>

    fun getAllSince(oldest: Instant): Flow<List<SkatGamePreview>>
    fun getAll(): Flow<List<SkatGamePreview>>
}
