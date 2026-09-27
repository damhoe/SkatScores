package com.damhoe.skatscores.game.skat.application.repository

import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import java.util.UUID

/**
 * Writing rounds only. A list's rounds are read as part of the list itself, through
 * [SkatGamesRepository], so there is no read here - the same shape the Doppelkopf scores
 * repository has always had.
 */
interface SkatScoresRepository
{
    /** Appends [score] as the newest round of the list. */
    suspend fun insert(score: SkatScore, gameId: UUID): Result<Unit>

    suspend fun update(score: SkatScore, gameId: UUID): Result<Unit>

    /**
     * Takes back the newest round of the list as the database has it. The id of the round
     * removed, or null when there was none.
     */
    suspend fun deleteLast(gameId: UUID): Result<UUID?>
}
