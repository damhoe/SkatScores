package com.damhoe.skatscores.game.doppelkopf.application.repository

import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import java.util.UUID

interface DoppelkopfScoresRepository
{
    suspend fun insert(score: DoppelkopfScore, gameId: UUID, round: Int): Result<Unit>
    suspend fun update(score: DoppelkopfScore, gameId: UUID): Result<Unit>
    suspend fun delete(id: UUID, gameId: UUID): Result<Unit>
}
