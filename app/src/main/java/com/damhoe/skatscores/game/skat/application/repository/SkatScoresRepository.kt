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
    suspend fun insert(score: SkatScore, gameId: UUID, round: Int): Result<Unit>
    suspend fun update(score: SkatScore, gameId: UUID): Result<Unit>
    suspend fun delete(id: UUID, gameId: UUID): Result<Unit>
}