package com.damhoe.skatscores.game.skat.application.usecases

import com.damhoe.skatscores.game.skat.application.repository.SkatGamesRepository
import com.damhoe.skatscores.game.skat.domain.SkatGame
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ObserveSkatGameUseCase @Inject constructor(
    private val repository: SkatGamesRepository,
)
{
    operator fun invoke(id: UUID): Flow<SkatGame?> = repository.observe(id)
}
