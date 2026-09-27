package com.damhoe.skatscores.game.doppelkopf.application.repository

import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import java.util.UUID

interface DoppelkopfScoresRepository
{
    /** Appends [score] as the newest round of the list. */
    suspend fun insert(score: DoppelkopfScore, gameId: UUID): Result<Unit>

    suspend fun update(score: DoppelkopfScore, gameId: UUID): Result<Unit>

    /**
     * Takes back the newest round of the list as the database has it. The id of the round
     * removed, or null when there was none.
     */
    suspend fun deleteLast(gameId: UUID): Result<UUID?>
}
