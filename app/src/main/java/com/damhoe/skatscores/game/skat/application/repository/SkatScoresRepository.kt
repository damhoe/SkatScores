package com.damhoe.skatscores.game.skat.application.repository

import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface SkatScoresRepository
{
    fun getScoresOfGame(gameId: UUID): Flow<SkatScore>
    suspend fun insert(score: SkatScore, gameId: UUID, round: Int): Result<Unit>
    suspend fun update(score: SkatScore, gameId: UUID): Result<Unit>
    suspend fun delete(id: UUID, gameId: UUID): Result<Unit>
}