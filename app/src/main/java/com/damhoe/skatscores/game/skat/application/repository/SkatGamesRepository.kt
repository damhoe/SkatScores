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

    /** Deletes the list with its seats and rounds. Deleting one that is gone is not an error. */
    suspend fun delete(id: UUID): Result<Unit>

    suspend fun updateSettings(id: UUID, title: Title, settings: SkatSettings): Result<Unit>

    /**
     * Who sits where. Each seat keeps its participant id, so the rounds already recorded stay
     * in their column; see UpdateSkatParticipantsUseCase.
     */
    suspend fun updateSeats(id: UUID, participants: SkatParticipants): Result<Unit>

    suspend fun get(id: UUID): Result<SkatGame?>

    /** The list as it is now and after every write to it; null once it is gone. */
    fun observe(id: UUID): Flow<SkatGame?>

    /** Both re-read on every write, so a collector stays in step with the database. */
    fun getAllSince(oldest: Instant): Flow<List<SkatGamePreview>>
    fun getAll(): Flow<List<SkatGamePreview>>
}
