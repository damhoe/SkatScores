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

    /** Deletes the list with its seats and rounds. Deleting one that is gone is not an error. */
    suspend fun delete(id: UUID): Result<Unit>

    suspend fun updateSettings(id: UUID, title: Title, settings: DoppelkopfSettings): Result<Unit>

    /** Who sits where; each seat keeps its participant id, so recorded rounds stay put. */
    suspend fun updateSeats(id: UUID, participants: DoppelkopfParticipants): Result<Unit>

    suspend fun get(id: UUID): Result<DoppelkopfGame?>

    /** The list as it is now and after every write to it; null once it is gone. */
    fun observe(id: UUID): Flow<DoppelkopfGame?>

    fun getAll(): Flow<List<DoppelkopfGamePreview>>
}
