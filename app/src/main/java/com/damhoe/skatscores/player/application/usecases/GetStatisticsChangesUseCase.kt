package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * When the numbers read off a player - lists played, statistics - have to be read again,
 * because a list or a round behind them was written.
 */
class GetStatisticsChangesUseCase @Inject constructor(
    private val repository: PlayerStatisticsRepository,
)
{
    operator fun invoke(): Flow<Unit> = repository.changes
}
