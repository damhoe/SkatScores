package com.damhoe.skatscores.game.doppelkopf.application.repository

import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import java.util.UUID

interface DoppelkopfScoresRepository
{
    suspend fun insert(score: DoppelkopfScore, gameId: UUID): Result<Unit>

    suspend fun update(score: DoppelkopfScore, gameId: UUID): Result<Unit>

    /** The id of the round removed, or null when the list had none. */
    suspend fun deleteLast(gameId: UUID): Result<UUID?>
}
