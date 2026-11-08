package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStatisticsChangesUseCase @Inject constructor(
    private val repository: PlayerStatisticsRepository,
)
{
    operator fun invoke(): Flow<Unit> = repository.changes
}
