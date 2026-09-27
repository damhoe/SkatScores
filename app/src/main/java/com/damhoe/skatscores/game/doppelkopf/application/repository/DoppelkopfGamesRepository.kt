package com.damhoe.skatscores.game.doppelkopf.application.repository

import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGamePreview
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSettings
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface DoppelkopfGamesRepository
{
    suspend fun save(game: DoppelkopfGame): Result<Unit>

    /** Deleting a missing list succeeds. */
    suspend fun delete(id: UUID): Result<Unit>

    suspend fun updateSettings(id: UUID, title: Title, settings: DoppelkopfSettings): Result<Unit>

    /** Seats keep their participant ids, so recorded rounds follow the new names. */
    suspend fun updateSeats(id: UUID, participants: DoppelkopfParticipants): Result<Unit>

    suspend fun get(id: UUID): Result<DoppelkopfGame?>

    /** Emits after every write to the list; null once it is gone. */
    fun observe(id: UUID): Flow<DoppelkopfGame?>

    fun getAll(): Flow<List<DoppelkopfGamePreview>>
}
