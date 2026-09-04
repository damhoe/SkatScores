package com.damhoe.skatscores.player.application.usecases

import com.damhoe.skatscores.player.application.repositories.PlayerStatisticsRepository
import com.damhoe.skatscores.player.domain.ListCounts
import java.util.UUID
import javax.inject.Inject

class GetListCountsUseCase @Inject constructor(
    private val repository: PlayerStatisticsRepository,
)
{
    suspend operator fun invoke(playerId: UUID): Result<ListCounts>
    {
        return repository.getListCounts(playerId)
    }
}
