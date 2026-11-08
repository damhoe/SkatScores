package com.damhoe.skatscores.game.skat.application.repository

import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import java.util.UUID

/** Writes only; rounds are read with their list through [SkatGamesRepository]. */
interface SkatScoresRepository
{
    suspend fun insert(score: SkatScore, gameId: UUID): Result<Unit>

    suspend fun update(score: SkatScore, gameId: UUID): Result<Unit>

    /** The id of the round removed, or null when the list had none. */
    suspend fun deleteLast(gameId: UUID): Result<UUID?>
}
